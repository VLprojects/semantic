package pro.vlprojects.semantic.delivery.outbox

public class OutboxVersionConflictException(id: OutboxId) :
    RuntimeException("Outbox $id was changed by another worker")
