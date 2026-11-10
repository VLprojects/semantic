package pro.vlprojects.semantic.traffic

import java.time.Duration
import java.time.Instant

public sealed interface RateLimitStrategy {

    public val permitLimit: Int

    public val window: Duration

    public fun tryAcquire(state: RateLimitState?, permits: Int, now: Instant): RateLimitResult
}
