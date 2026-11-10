package pro.vlprojects.semantic.traffic.limit.rate

import java.time.Duration

@ConsistentCopyVisibility
public data class RateLimitDecision private constructor(
    public val outcome: Outcome,
    public val tokens: Capacity,
    public val retryAfter: Duration,
    public val resetAfter: Duration,
) {

    public enum class Outcome {
        PASSED,
        REJECTED,
    }

    @ConsistentCopyVisibility
    public data class Capacity private constructor(
        public val total: Int,
        public val remaining: Int,
    ) {

        public val used: Int
            get() = total - remaining

        public companion object Factory {

            public fun of(total: Int, remaining: Int): Capacity = Capacity(total, remaining)
        }
    }

    public companion object Factory {

        public fun passed(tokens: Capacity, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(Outcome.PASSED, tokens, retryAfter = Duration.ZERO, resetAfter)

        public fun rejected(tokens: Capacity, retryAfter: Duration, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(Outcome.REJECTED, tokens, retryAfter, resetAfter)
    }
}
