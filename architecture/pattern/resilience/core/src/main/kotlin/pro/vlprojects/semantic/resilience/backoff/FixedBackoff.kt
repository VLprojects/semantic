package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

public class FixedBackoff(private val delay: Duration) : Backoff {

    init {
        require(delay.isPositive) { "The delay must be positive: $delay" }
    }

    override fun calculateDelay(attempt: Int): Duration = attempt
        .also(::requirePositiveAttempt)
        .let { delay }

    public companion object Factory {

        private val DEFAULT_DELAY = Duration.ofSeconds(1)

        public fun of(delay: Duration = DEFAULT_DELAY): FixedBackoff = FixedBackoff(delay)

        public fun ofMillis(millis: Long): FixedBackoff = millis
            .let(Duration::ofMillis)
            .let(::FixedBackoff)

        public fun ofSeconds(seconds: Long): FixedBackoff = seconds
            .let(Duration::ofSeconds)
            .let(::FixedBackoff)

        public fun ofMinutes(minutes: Long): FixedBackoff = minutes
            .let(Duration::ofMinutes)
            .let(::FixedBackoff)
    }
}
