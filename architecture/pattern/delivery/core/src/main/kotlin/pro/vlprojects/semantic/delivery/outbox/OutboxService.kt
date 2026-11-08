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
        .let(repository::save)

    public fun retry(outbox: Outbox, cause: Throwable, delay: Duration): Outbox {
        val scheduledAt = clock.instant().plus(delay)
        val error = errorPolicy.extract(cause)
        return outbox
            .fail(scheduledAt, error)
            .let(repository::save)
    }

    public fun cancel(outbox: Outbox, cause: Throwable): Outbox {
        val now = clock.instant()
        val error = errorPolicy.extract(cause)
        return outbox
            .fail(now, error)
            .cancel(now)
            .let(repository::save)
    }

    private fun tryLock(outbox: Outbox) = try {
        clock
            .instant()
            .plus(lockDuration)
            .let(outbox::start)
            .let(repository::save)
    } catch (_: OutboxOptimisticLockException) {
        null
    }
}
