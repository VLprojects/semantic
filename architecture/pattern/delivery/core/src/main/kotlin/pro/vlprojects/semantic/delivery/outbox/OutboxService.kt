package pro.vlprojects.semantic.delivery.outbox

import java.time.Clock
import java.time.Duration

public class OutboxService(
    private val repository: OutboxRepository,
    private val lockDuration: Duration,
    private val clock: Clock,
    private val errorPolicy: ErrorPolicy = RootCauseErrorPolicy,
) {

    public fun getAllWithLock(limit: Int): List<Outbox> = clock
        .instant()
        .let { repository.findScheduledForDelivery(it, limit) }
        .mapNotNull(::tryLock)

    public fun complete(outbox: Outbox): Outbox = clock
        .instant()
        .let(outbox::complete)
        .let { repository.save(outbox) }

    public fun retry(outbox: Outbox, cause: Throwable, delay: Duration): Outbox {
        val scheduledAt = clock.instant().plus(delay)
        val error = errorPolicy.extract(cause)
        outbox.fail(scheduledAt, error)
        return repository.save(outbox)
    }

    public fun cancel(outbox: Outbox, cause: Throwable): Outbox {
        val now = clock.instant()
        val error = errorPolicy.extract(cause)
        outbox.fail(now, error)
        outbox.cancel(now)
        return repository.save(outbox)
    }

    private fun tryLock(outbox: Outbox) = try {
        clock
            .instant()
            .plus(lockDuration)
            .let(outbox::start)
            .let { repository.save(outbox) }
    } catch (_: OutboxOptimisticLockException) {
        null
    }
}
