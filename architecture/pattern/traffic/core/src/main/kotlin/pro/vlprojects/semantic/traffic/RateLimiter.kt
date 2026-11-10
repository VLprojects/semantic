package pro.vlprojects.semantic.traffic

public interface RateLimiter {

    public val name: String

    public fun tryAcquire(key: String, permits: Int = 1): RateLimitResponse
}
