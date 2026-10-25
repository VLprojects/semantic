package pro.vlprojects.semantic.events

import kotlin.time.Duration

public class FixedRetryPolicy(
    private val delay: Duration,
    private val maxAttempts: Int,
) : RetryPolicy {

    init {
        require(delay.isPositive()) { "The delay must be positive: $delay" }
        require(maxAttempts > 0) { "The number of attempts must be positive: $maxAttempts" }
    }

    override fun delayAfter(attempt: Int): Duration? = delay.takeIf { attempt < maxAttempts }
}
