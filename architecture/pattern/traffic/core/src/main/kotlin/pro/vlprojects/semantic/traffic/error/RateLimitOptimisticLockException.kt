package pro.vlprojects.semantic.traffic.error

public class RateLimitOptimisticLockException(key: String) :
    ConcurrentModificationException("Rate limit counter $key was changed concurrently")
