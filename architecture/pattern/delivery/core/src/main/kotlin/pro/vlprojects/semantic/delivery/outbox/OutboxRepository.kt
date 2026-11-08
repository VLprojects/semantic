package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public interface OutboxRepository {

    public fun save(outbox: Outbox): Outbox

    public fun findById(id: OutboxId): Outbox?

    public fun lockScheduledForDelivery(scheduledUntil: Instant, lockedUntil: Instant, limit: Int): List<Outbox>

    public fun deleteAll(status: DeliveryStatus, terminatedBefore: Instant, limit: Int): Int
}
