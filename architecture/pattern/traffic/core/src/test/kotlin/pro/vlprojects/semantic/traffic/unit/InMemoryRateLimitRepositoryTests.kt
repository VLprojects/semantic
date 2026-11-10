package pro.vlprojects.semantic.traffic.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.traffic.InMemoryRateLimitRepository
import pro.vlprojects.semantic.traffic.RateLimitCounter
import pro.vlprojects.semantic.traffic.TokenBucketState
import pro.vlprojects.semantic.traffic.error.RateLimitOptimisticLockException
import java.time.Duration
import java.time.Instant

class InMemoryRateLimitRepositoryTests {

    private val moment = Instant.parse("2026-11-10T00:00:00Z")
    private val clock = MutableClock(moment)
    private val repository = InMemoryRateLimitRepository(clock)
    private val state = TokenBucketState.of(1, moment)
    private val counter = RateLimitCounter.of("api:client", state, version = 0, expiresAt = moment.plusSeconds(1))

    @Test
    fun `saving increments the version`() {
        val saved = repository.save(counter)

        assertEquals(1, saved.version)
        assertEquals(saved, repository.findByKey("api:client"))
    }

    @Test
    fun `saving a stale version fails`() {
        repository.save(counter)

        assertThrows<RateLimitOptimisticLockException> { repository.save(counter) }
    }

    @Test
    fun `an expired counter is dropped and can be created again`() {
        repository.save(counter)
        clock.add(Duration.ofSeconds(1))

        assertNull(repository.findByKey("api:client"))
        assertEquals(1, repository.save(counter).version)
    }
}
