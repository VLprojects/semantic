package pro.vlprojects.semantic.traffic.limit.rate

public interface RateLimiter {

    public fun allow(key: String, tokens: Int = 1): RateLimitDecision
}
