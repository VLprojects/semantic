package pro.vlprojects.semantic.traffic.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.traffic.DefaultRateLimiter
import pro.vlprojects.semantic.traffic.InMemoryRateLimitRepository
import pro.vlprojects.semantic.traffic.RateLimitCounter
import pro.vlprojects.semantic.traffic.RateLimitFailureMode
import pro.vlprojects.semantic.traffic.RateLimitRepository
import pro.vlprojects.semantic.traffic.TokenBucket
import pro.vlprojects.semantic.traffic.TokenBucketState
import pro.vlprojects.semantic.traffic.error.RateLimitOptimisticLockException
import java.time.Duration
import java.time.Instant

class DefaultRateLimiterTests {

    private val moment = Instant.parse("2026-11-10T00:00:00Z")
    private val clock = MutableClock(moment)
    private val memory = InMemoryRateLimitRepository(clock)
    private val savedCounters = mutableListOf<RateLimitCounter>()
    private var conflicts = 0
    private var isUnavailable = false

    private val repository = object : RateLimitRepository {

        override fun findByKey(key: String): RateLimitCounter? {
            check(!isUnavailable) { "The storage is down" }

            return memory.findByKey(key)
        }

        override fun save(counter: RateLimitCounter): RateLimitCounter {
            if (conflicts > 0) {
                conflicts--
                throw RateLimitOptimisticLockException(counter.key)
            }

            return memory
                .save(counter)
                .also(savedCounters::add)
        }
    }

    private fun createLimiter(onFailure: RateLimitFailureMode = RateLimitFailureMode.ALLOW) = DefaultRateLimiter(
        name = "api",
        algorithm = TokenBucket.of(permitLimit = 2, window = Duration.ofSeconds(2)),
        repository = repository,
        clock = clock,
        maxAttempts = 3,
        onFailure = onFailure,
    )

    @Test
    fun `an acquired permit saves the counter of the limiter until it is full again`() {
        val expected = RateLimitCounter.of("api:client", TokenBucketState.of(1, moment), 1, moment.plusSeconds(1))

        val response = createLimiter().tryAcquire("client")

        assertTrue(response.isAcquired)
        assertEquals(listOf(expected), savedCounters)
    }

    @Test
    fun `a rejection saves nothing`() {
        val limiter = createLimiter()
        limiter.tryAcquire("client", 2)

        val response = limiter.tryAcquire("client")

        assertFalse(response.isAcquired)
        assertEquals(1, savedCounters.size)
    }

    @Test
    fun `a conflict of versions is repeated`() {
        conflicts = 2

        val response = createLimiter().tryAcquire("client")

        assertTrue(response.isAcquired)
        assertEquals(1, savedCounters.size)
    }

    @Test
    fun `conflicts on every attempt allow the call`() {
        conflicts = 3

        val response = createLimiter().tryAcquire("client")

        assertTrue(response.isAcquired)
        assertEquals(emptyList<RateLimitCounter>(), savedCounters)
    }

    @Test
    fun `a storage failure rejects the call when the failure mode is reject`() {
        isUnavailable = true

        val response = createLimiter(RateLimitFailureMode.REJECT).tryAcquire("client")

        assertFalse(response.isAcquired)
    }

    @Test
    fun `permits over the limit are an error of the caller`() {
        assertThrows<IllegalArgumentException> { createLimiter().tryAcquire("client", 3) }
    }
}
