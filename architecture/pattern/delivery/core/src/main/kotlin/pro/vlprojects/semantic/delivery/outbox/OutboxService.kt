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
        .let(outbox::markCompleted)
        .let(repository::save)

    public fun retryDelivery(outbox: Outbox, error: String, delay: Duration): Outbox = clock
        .instant()
        .plus(delay)
        .let { outbox.markFailed(it, error) }
        .let(repository::save)

    public fun stopDelivery(outbox: Outbox, error: String): Outbox = error
        .let(outbox::markAbandoned)
        .let(repository::save)

    private fun tryLock(outbox: Outbox) = try {
        clock
            .instant()
            .plus(lockDuration)
            .let(outbox::markInProgress)
            .let(repository::save)
    } catch (_: OutboxVersionConflictException) {
        null
    }
}
