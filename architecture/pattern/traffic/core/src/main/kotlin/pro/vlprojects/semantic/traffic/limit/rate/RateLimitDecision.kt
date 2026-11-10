package pro.vlprojects.semantic.traffic.limit.rate

import java.time.Duration

@ConsistentCopyVisibility
public data class RateLimitDecision private constructor(
    public val status: RateLimitStatus,
    public val remaining: Int,
    public val retryAfter: Duration,
    public val resetAfter: Duration,
) {

    public companion object Factory {

        public fun passed(remaining: Int, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(RateLimitStatus.PASSED, remaining, retryAfter = Duration.ZERO, resetAfter)

        public fun rejected(remaining: Int, retryAfter: Duration, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(RateLimitStatus.REJECTED, remaining, retryAfter, resetAfter)
    }
}
