package pro.vlprojects.semantic.delivery.outbox

import java.time.Clock
import java.time.Duration

public class OutboxService(
    private val repository: OutboxRepository,
    private val lockDuration: Duration,
    private val clock: Clock,
) {

    public fun claimDue(limit: Int): List<Outbox> = clock
        .instant()
        .let { repository.findDue(it, limit) }
        .mapNotNull(::claimOrSkip)

    public fun complete(outbox: Outbox): Outbox = clock
        .instant()
        .let(outbox::completeDelivery)
        .let(repository::save)

    public fun reschedule(outbox: Outbox, reason: String, delay: Duration): Outbox = clock
        .instant()
        .plus(delay)
        .let { outbox.failDelivery(it, reason) }
        .let(repository::save)

    public fun abandon(outbox: Outbox, reason: String): Outbox = reason
        .let(outbox::abandonDelivery)
        .let(repository::save)

    private fun claimOrSkip(outbox: Outbox) = try {
        clock
            .instant()
            .plus(lockDuration)
            .let(outbox::startDelivery)
            .let(repository::save)
    } catch (_: OutboxVersionConflictException) {
        null
    }
}
