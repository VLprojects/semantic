package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

public class FixedBackoff(private val delay: Duration) : Backoff {

    init {
        require(delay.isPositive) { "The delay must be positive: $delay" }
    }

    override fun calculate(attempt: Int): Duration = attempt
        .also(::requirePositive)
        .let { delay }

    private fun requirePositive(attempt: Int) = require(attempt >= 1) { "The attempt must be positive: $attempt" }

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
