package pro.vlprojects.semantic.traffic

import java.time.Instant

@ConsistentCopyVisibility
public data class RateLimitCounter private constructor(
    public val key: String,
    public val state: RateLimitState,
    public val version: Long,
    public val expiresAt: Instant,
) {

    internal fun isExpired(now: Instant) = expiresAt <= now

    internal fun incrementVersion() = copy(version = version + 1)

    public companion object Factory {

        public fun of(key: String, state: RateLimitState, version: Long, expiresAt: Instant): RateLimitCounter =
            RateLimitCounter(key, state, version, expiresAt)
    }
}
