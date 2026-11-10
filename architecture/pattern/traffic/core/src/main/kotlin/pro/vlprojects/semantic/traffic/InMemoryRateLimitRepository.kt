package pro.vlprojects.semantic.traffic

import pro.vlprojects.semantic.traffic.error.RateLimitOptimisticLockException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

private val CLEAN_UP_INTERVAL = Duration.ofMinutes(1)

public class InMemoryRateLimitRepository(private val clock: Clock = Clock.systemUTC()) : RateLimitRepository {

    private val counters = ConcurrentHashMap<String, RateLimitCounter>()
    private val cleanUpAt = clock
        .instant()
        .plus(CLEAN_UP_INTERVAL)
        .let(::AtomicReference)

    override fun findByKey(key: String): RateLimitCounter? {
        val now = clock.instant()

        return counters.computeIfPresent(key) { _, counter ->
            counter.takeUnless { stored -> stored.isExpired(now) }
        }
    }

    override fun save(counter: RateLimitCounter): RateLimitCounter {
        val now = clock.instant()
        val saved = counter.incrementVersion()
        counters.compute(counter.key) { _, stored ->
            checkVersion(stored, counter, now)
            saved
        }
        cleanUpExpiredCounters(now)

        return saved
    }

    private fun checkVersion(stored: RateLimitCounter?, counter: RateLimitCounter, now: Instant) {
        val version = stored
            ?.takeUnless { current -> current.isExpired(now) }
            ?.version
            ?: 0L

        if (version != counter.version) {
            throw RateLimitOptimisticLockException(counter.key)
        }
    }

    // Time: O(n) once a minute, so counters of keys no longer used do not pile up.
    private fun cleanUpExpiredCounters(now: Instant) {
        val scheduledAt = cleanUpAt.get()
        val nextScheduledAt = now.plus(CLEAN_UP_INTERVAL)

        if (now >= scheduledAt && cleanUpAt.compareAndSet(scheduledAt, nextScheduledAt)) {
            counters.values.removeIf { counter -> counter.isExpired(now) }
        }
    }
}
