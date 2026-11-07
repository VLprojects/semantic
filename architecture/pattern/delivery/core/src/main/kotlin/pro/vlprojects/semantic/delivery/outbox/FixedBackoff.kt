package pro.vlprojects.semantic.delivery.outbox

import java.time.Duration

public class FixedBackoff(private val delay: Duration) : Backoff {

    init {
        require(delay.isPositive) { "The delay must be positive: $delay" }
    }

    override fun delayAfter(attempt: Int): Duration = delay
}
