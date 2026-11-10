package pro.vlprojects.semantic.traffic.limit.rate

@ConsistentCopyVisibility
public data class RateLimitResult private constructor(
    public val state: RateLimitState,
    public val decision: RateLimitDecision,
) {

    public companion object Factory {

        public fun of(state: RateLimitState, decision: RateLimitDecision): RateLimitResult =
            RateLimitResult(state, decision)
    }
}
