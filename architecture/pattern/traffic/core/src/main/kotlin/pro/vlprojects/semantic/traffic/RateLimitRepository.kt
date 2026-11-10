package pro.vlprojects.semantic.traffic

import pro.vlprojects.semantic.traffic.error.RateLimitOptimisticLockException

public interface RateLimitRepository {

    public fun findByKey(key: String): RateLimitCounter?

    @Throws(RateLimitOptimisticLockException::class)
    public fun save(counter: RateLimitCounter): RateLimitCounter
}
