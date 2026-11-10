package pro.vlprojects.semantic.traffic.limit.rate

import java.time.Duration
import java.time.Instant

public sealed interface RateLimitStrategy {

    public fun allow(
        state: RateLimitState?,
        tokens: Int,
        now: Instant,
        maxWait: Duration = Duration.ZERO,
    ): RateLimitResult
}
