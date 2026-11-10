package pro.vlprojects.semantic.traffic.limit.rate

public interface RateLimiter {

    public val name: String

    public fun allow(key: String, cost: Int = 1): RateLimitDecision
}
