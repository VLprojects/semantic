package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxConflictException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException
import pro.vlprojects.semantic.delivery.outbox.id.OutboxId
import java.time.Instant

public interface OutboxRepository {

    @Throws(OutboxConflictException::class, OutboxOptimisticLockException::class)
    public fun save(outbox: Outbox): Outbox

    public fun findById(id: OutboxId): Outbox?

    public fun findAndModifyScheduled(startedAt: Instant, limit: Int): List<Outbox>

    public fun findAllProcessing(startedBefore: Instant, limit: Int): List<Outbox>

    public fun deleteAll(status: DeliveryStatus, terminatedBefore: Instant, limit: Int): Int
}
