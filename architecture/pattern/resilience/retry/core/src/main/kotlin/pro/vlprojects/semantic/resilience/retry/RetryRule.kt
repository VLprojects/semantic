package pro.vlprojects.semantic.resilience.retry

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import java.time.Duration
import java.util.concurrent.CancellationException

public class RetryRule(
    private val backoff: Backoff,
    private val attemptLimit: Int,
    private val timeLimit: Duration?,
    private val condition: RetryCondition,
) {

    init {
        require(attemptLimit >= 1) { "The attempt limit must be positive: $attemptLimit" }
        require(timeLimit?.isPositive ?: true) { "The time limit must be positive: $timeLimit" }
    }

    public fun decide(attempt: Int, elapsed: Duration, failure: Throwable): RetryDecision = when {
        failure is InterruptedException -> RetryDecision.Stop(RetryStopReason.INTERRUPTED)
        failure.isFatal() -> RetryDecision.Stop(RetryStopReason.NOT_RETRYABLE)
        !condition.matches(failure) -> RetryDecision.Stop(RetryStopReason.NOT_RETRYABLE)
        attempt >= attemptLimit -> RetryDecision.Stop(RetryStopReason.ATTEMPTS_EXHAUSTED)
        else -> decideDelay(attempt, elapsed)
    }

    private fun Throwable.isFatal() = when (this) {
        is Error -> true
        is CancellationException -> true
        else -> false
    }

    private fun decideDelay(attempt: Int, elapsed: Duration) = attempt
        .let(backoff::calculate)
        .let { delay ->
            when {
                isWithinTimeLimit(elapsed, delay) -> RetryDecision.Wait(delay)
                else -> RetryDecision.Stop(RetryStopReason.TIME_EXHAUSTED)
            }
        }

    private fun isWithinTimeLimit(elapsed: Duration, delay: Duration) = timeLimit
        ?.let { limit -> elapsed + delay <= limit }
        ?: true

    public companion object Factory {

        private const val DEFAULT_ATTEMPT_LIMIT = 3

        public fun of(
            backoff: Backoff = ExponentialBackoff.ofRandomized(),
            attemptLimit: Int = DEFAULT_ATTEMPT_LIMIT,
            timeLimit: Duration? = null,
            condition: RetryCondition = RetryCondition.ofAny(),
        ): RetryRule = RetryRule(backoff, attemptLimit, timeLimit, condition)
    }
}
