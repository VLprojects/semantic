package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

public class FixedBackoff(
    private val delay: Duration,
    override val maxAttempts: Int? = null,
    override val deadline: Duration? = null,
) : Backoff {

    init {
        require(delay.isPositive) { "The delay must be positive: $delay" }
    }

    private val delayNanos = delay.toNanos().toDouble()
    private val limits = BackoffLimits(maxAttempts, deadline)

    override fun calculateDelay(attempt: Int): Duration = attempt
        .also(::requirePositiveAttempt)
        .also(::checkLimits)
        .let { delay }

    private fun checkLimits(attempt: Int) = attempt
        .times(delayNanos)
        .let { waitedNanos -> limits.check(attempt, waitedNanos) }

    public companion object Factory {

        private val DEFAULT_DELAY = Duration.ofSeconds(1)

        public fun of(
            delay: Duration = DEFAULT_DELAY,
            maxAttempts: Int? = null,
            deadline: Duration? = null,
        ): FixedBackoff = FixedBackoff(delay, maxAttempts, deadline)

        public fun ofMillis(
            millis: Long,
            maxAttempts: Int? = null,
            deadline: Duration? = null,
        ): FixedBackoff = millis
            .let(Duration::ofMillis)
            .let { delay -> FixedBackoff(delay, maxAttempts, deadline) }

        public fun ofSeconds(
            seconds: Long,
            maxAttempts: Int? = null,
            deadline: Duration? = null,
        ): FixedBackoff = seconds
            .let(Duration::ofSeconds)
            .let { delay -> FixedBackoff(delay, maxAttempts, deadline) }

        public fun ofMinutes(
            minutes: Long,
            maxAttempts: Int? = null,
            deadline: Duration? = null,
        ): FixedBackoff = minutes
            .let(Duration::ofMinutes)
            .let { delay -> FixedBackoff(delay, maxAttempts, deadline) }
    }
}
