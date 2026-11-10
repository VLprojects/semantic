package pro.vlprojects.semantic.traffic.limit.rate

import java.time.Duration

@ConsistentCopyVisibility
public data class RateLimitDecision private constructor(
    public val isAllowed: Boolean,
    public val remaining: Int,
    public val retryAfter: Duration,
    public val resetAfter: Duration,
) {

    public companion object Factory {

        public fun allowed(remaining: Int, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(isAllowed = true, remaining, retryAfter = Duration.ZERO, resetAfter)

        public fun rejected(remaining: Int, retryAfter: Duration, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(isAllowed = false, remaining, retryAfter, resetAfter)
    }
}
