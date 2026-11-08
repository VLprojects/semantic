package pro.vlprojects.semantic.resilience.retry

import pro.vlprojects.semantic.resilience.policy.Policy
import java.time.Duration
import java.time.Instant
import java.time.InstantSource

public class Retry(
    private val name: String,
    private val rule: RetryRule,
    private val listener: RetryListener,
    private val sleeper: Sleeper,
    private val instantSource: InstantSource,
) : Policy {

    init {
        require(name.isNotBlank()) { "The name must not be blank" }
    }

    override fun <T> execute(action: () -> T): T {
        val startedAt = instantSource.instant()
        var attempt = 1
        while (true) {
            val outcome = runCatching(action)
            val progress = RetryProgress(name, attempt, readElapsed(startedAt))
            val failure = outcome.exceptionOrNull() ?: return outcome.getOrThrow().also { listener.onSuccess(progress) }
            awaitNextAttempt(progress, failure)
            attempt++
        }
    }

    private fun readElapsed(startedAt: Instant) = instantSource
        .instant()
        .let { now -> Duration.between(startedAt, now) }

    private fun awaitNextAttempt(progress: RetryProgress, failure: Throwable) {
        when (val decision = decide(progress, failure)) {
            is RetryDecision.Wait -> wait(progress, failure, decision.delay)
            is RetryDecision.Stop -> stop(progress, failure, decision.reason)
        }
    }

    private fun decide(progress: RetryProgress, failure: Throwable) = when {
        Thread.currentThread().isInterrupted -> RetryDecision.Stop(RetryStopReason.INTERRUPTED)
        else -> rule.decide(progress.attempt, progress.elapsed, failure)
    }

    private fun wait(progress: RetryProgress, failure: Throwable, delay: Duration) {
        listener.onRetry(progress, failure, delay)
        try {
            sleeper.sleep(delay)
        } catch (_: InterruptedException) {
            stop(progress, failure, RetryStopReason.INTERRUPTED)
        }
    }

    private fun stop(progress: RetryProgress, failure: Throwable, reason: RetryStopReason): Nothing {
        if (reason == RetryStopReason.INTERRUPTED) {
            Thread.currentThread().interrupt()
        }
        listener.onStop(progress, failure, reason)
        throw failure
    }

    public companion object Factory {

        public fun of(
            name: String,
            rule: RetryRule = RetryRule.of(),
            listener: RetryListener = RetryListener.ofSilent(),
            sleeper: Sleeper = Sleeper.ofThread(),
            instantSource: InstantSource = InstantSource.system(),
        ): Retry = Retry(name, rule, listener, sleeper, instantSource)
    }
}
