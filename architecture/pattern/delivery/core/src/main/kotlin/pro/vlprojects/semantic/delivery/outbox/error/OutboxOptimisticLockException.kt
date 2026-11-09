package pro.vlprojects.semantic.delivery.outbox.error

import pro.vlprojects.semantic.delivery.outbox.id.OutboxId

public class OutboxOptimisticLockException(id: OutboxId) :
    ConcurrentModificationException("Outbox $id was changed concurrently")
