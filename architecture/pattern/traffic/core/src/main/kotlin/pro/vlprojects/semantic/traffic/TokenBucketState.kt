package pro.vlprojects.semantic.traffic

import java.time.Instant

@ConsistentCopyVisibility
public data class TokenBucketState private constructor(
    public val tokens: Int,
    public val refilledAt: Instant,
) : RateLimitState {

    public companion object Factory {

        public fun of(tokens: Int, refilledAt: Instant): TokenBucketState = TokenBucketState(tokens, refilledAt)
    }
}
