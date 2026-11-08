package pro.vlprojects.semantic.delivery.outbox

import java.time.Clock
import java.time.Duration

public class OutboxService(
    private val repository: OutboxRepository,
    private val lockDuration: Duration,
    private val clock: Clock,
    private val errorPolicy: ErrorPolicy = RootCauseErrorPolicy,
) {

    public fun getAllWithLock(limit: Int): List<Outbox> {
        val now = clock.instant()
        val lockedUntil = now.plus(lockDuration)

        return repository.lockScheduledForDelivery(now, lockedUntil, limit)
    }

    public fun complete(outbox: Outbox): Outbox = clock
        .instant()
        .let(outbox::complete)
        .let(repository::save)

    public fun reschedule(outbox: Outbox, cause: Throwable, delay: Duration): Outbox {
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
}
