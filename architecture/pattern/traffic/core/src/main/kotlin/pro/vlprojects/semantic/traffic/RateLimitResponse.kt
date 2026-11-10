package pro.vlprojects.semantic.traffic

import java.time.Duration

@ConsistentCopyVisibility
public data class RateLimitResponse private constructor(
    public val isAcquired: Boolean,
    public val delay: Duration,
    public val remaining: Int,
    public val retryAfter: Duration,
    public val resetAfter: Duration,
) {

    public companion object Factory {

        public fun acquired(remaining: Int, resetAfter: Duration): RateLimitResponse =
            RateLimitResponse(isAcquired = true, Duration.ZERO, remaining, retryAfter = Duration.ZERO, resetAfter)

        public fun rejected(remaining: Int, retryAfter: Duration, resetAfter: Duration): RateLimitResponse =
            RateLimitResponse(isAcquired = false, Duration.ZERO, remaining, retryAfter, resetAfter)
    }
}
