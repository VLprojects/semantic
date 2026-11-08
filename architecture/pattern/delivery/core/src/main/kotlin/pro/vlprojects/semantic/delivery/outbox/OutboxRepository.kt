package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public interface OutboxRepository {

    public fun save(outbox: Outbox): Outbox

    public fun findById(id: OutboxId): Outbox?

    public fun findDue(now: Instant, limit: Int): List<Outbox>

    public fun deleteCompletedBefore(instant: Instant, limit: Int): Int
}
