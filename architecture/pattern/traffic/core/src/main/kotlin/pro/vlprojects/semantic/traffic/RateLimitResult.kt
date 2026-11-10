package pro.vlprojects.semantic.traffic

@ConsistentCopyVisibility
public data class RateLimitResult private constructor(
    public val state: RateLimitState,
    public val response: RateLimitResponse,
) {

    public companion object Factory {

        public fun of(state: RateLimitState, response: RateLimitResponse): RateLimitResult =
            RateLimitResult(state, response)
    }
}
