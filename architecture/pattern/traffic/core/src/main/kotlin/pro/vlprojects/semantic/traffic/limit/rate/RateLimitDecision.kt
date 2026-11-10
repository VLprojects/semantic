package pro.vlprojects.semantic.traffic.limit.rate

import java.time.Duration
import java.time.Instant

@ConsistentCopyVisibility
public data class RateLimitDecision private constructor(
    public val outcome: Outcome,
    public val tokens: Capacity,
    public val delay: Duration,
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
            passed(tokens, delay = Duration.ZERO, resetAfter)

        public fun passed(tokens: Capacity, delay: Duration, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(Outcome.PASSED, tokens, delay, retryAfter = Duration.ZERO, resetAfter)

        public fun passed(
            tokens: Capacity,
            now: Instant,
            resetAt: Instant,
        ): RateLimitDecision {
            val resetAfter = Duration.between(now, resetAt)

            return passed(tokens, resetAfter)
        }

        public fun rejected(tokens: Capacity, retryAfter: Duration, resetAfter: Duration): RateLimitDecision =
            RateLimitDecision(Outcome.REJECTED, tokens, delay = Duration.ZERO, retryAfter, resetAfter)

        public fun rejected(
            tokens: Capacity,
            now: Instant,
            retryAt: Instant,
            resetAt: Instant,
        ): RateLimitDecision {
            val retryAfter = Duration.between(now, retryAt)
            val resetAfter = Duration.between(now, resetAt)

            return rejected(tokens, retryAfter, resetAfter)
        }
    }
}
