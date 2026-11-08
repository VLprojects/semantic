package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

public class FixedBackoff(private val delay: Duration) : Backoff {

    init {
        require(delay.isPositive) { "The delay must be positive: $delay" }
    }

    override fun delayAfter(attempt: Int): Duration {
        require(attempt >= 1) { "The attempt must be positive: $attempt" }

        return delay
    }
}
