package pro.vlprojects.semantic.delivery.outbox

public class OutboxOptimisticLockException(id: OutboxId) :
    ConcurrentModificationException("Outbox $id was changed by another worker")
