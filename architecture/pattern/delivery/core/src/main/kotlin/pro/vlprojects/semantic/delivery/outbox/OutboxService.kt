package pro.vlprojects.semantic.delivery.outbox

import java.time.Clock
import java.time.Duration

public class OutboxService(
    private val repository: OutboxRepository,
    private val lockDuration: Duration,
    private val clock: Clock,
) {

    public fun lockForDelivery(limit: Int): List<Outbox> = clock
        .instant()
        .let { repository.findReadyForDelivery(it, limit) }
        .mapNotNull(::tryLock)

    public fun completeDelivery(outbox: Outbox): Outbox = clock
        .instant()
        .let(outbox::complete)
        .let { repository.save(outbox) }

    public fun retryDelivery(outbox: Outbox, cause: Throwable, delay: Duration): Outbox = clock
        .instant()
        .plus(delay)
        .let { outbox.fail(it, cause) }
        .let { repository.save(outbox) }

    public fun cancelDelivery(outbox: Outbox, cause: Throwable): Outbox {
        val now = clock.instant()
        outbox.fail(now, cause)
        outbox.cancel(now)
        return repository.save(outbox)
    }

    private fun tryLock(outbox: Outbox) = try {
        clock
            .instant()
            .plus(lockDuration)
            .let(outbox::start)
            .let { repository.save(outbox) }
    } catch (_: OutboxVersionConflictException) {
        null
    }
}
