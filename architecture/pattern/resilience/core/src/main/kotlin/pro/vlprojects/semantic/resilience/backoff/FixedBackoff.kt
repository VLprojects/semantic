package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

public class FixedBackoff(private val delay: Duration) : Backoff {

    init {
        require(delay.isPositive) { "The delay must be positive: $delay" }
    }

    override fun delayAfter(attempt: Int): Duration = attempt
        .also(::requirePositiveAttempt)
        .let { delay }

    public companion object Factory {

        public fun every(delay: Duration): FixedBackoff = FixedBackoff(delay)

        public fun everyMillis(millis: Long): FixedBackoff = millis
            .let(Duration::ofMillis)
            .let(::every)

        public fun everySeconds(seconds: Long): FixedBackoff = seconds
            .let(Duration::ofSeconds)
            .let(::every)

        public fun everyMinutes(minutes: Long): FixedBackoff = minutes
            .let(Duration::ofMinutes)
            .let(::every)
    }
}
