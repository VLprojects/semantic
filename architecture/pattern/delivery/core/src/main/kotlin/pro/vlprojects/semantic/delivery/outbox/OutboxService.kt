package pro.vlprojects.semantic.delivery.outbox

import java.time.Clock

public class OutboxService(
    private val repository: OutboxRepository,
    private val properties: OutboxProperties,
    private val clock: Clock,
) {

    public fun getAllWithLock(limit: Int): List<Outbox> {
        val now = clock.instant()
        val lockedUntil = now.plus(properties.lockDuration)

        return repository.lockScheduledForDelivery(now, lockedUntil, limit)
    }

    public fun complete(outbox: Outbox): Outbox = clock
        .instant()
        .let(outbox::complete)
        .let(repository::save)

    public fun fail(outbox: Outbox, cause: Throwable): Outbox {
        val error = properties.errorPolicy.extract(cause)

        return when {
            hasAttemptsLeft(outbox) -> reschedule(outbox, error)
            else -> cancel(outbox, error)
        }.let(repository::save)
    }

    private fun hasAttemptsLeft(outbox: Outbox) = outbox.delivery.attempts < properties.maxAttempts

    private fun reschedule(outbox: Outbox, error: String): Outbox {
        val delay = properties.backoff.calculate(outbox.delivery.attempts)
        val scheduledAt = clock.instant().plus(delay)

        return outbox.fail(scheduledAt, error)
    }

    private fun cancel(outbox: Outbox, error: String): Outbox {
        val now = clock.instant()

        return outbox
            .fail(now, error)
            .cancel(now)
    }
}
