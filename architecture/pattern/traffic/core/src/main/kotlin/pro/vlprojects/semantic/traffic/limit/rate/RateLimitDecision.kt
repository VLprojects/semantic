package pro.vlprojects.semantic.traffic.limit.rate

import java.time.Duration

@ConsistentCopyVisibility
public data class RateLimitDecision private constructor(
    public val outcome: RateLimitOutcome,
    public val limit: Int,
    public val remainingTokens: Int,
    public val retryAfter: Duration,
    public val resetAfter: Duration,
) {

    public companion object Factory {

        public fun passed(
            limit: Int,
            remainingTokens: Int,
            resetAfter: Duration,
        ): RateLimitDecision =
            RateLimitDecision(RateLimitOutcome.PASSED, limit, remainingTokens, retryAfter = Duration.ZERO, resetAfter)

        public fun rejected(
            limit: Int,
            remainingTokens: Int,
            retryAfter: Duration,
            resetAfter: Duration,
        ): RateLimitDecision = RateLimitDecision(
            RateLimitOutcome.REJECTED,
            limit,
            remainingTokens,
            retryAfter,
            resetAfter,
        )
    }
}
