package pro.vlprojects.semantic.delivery.outbox.error

import pro.vlprojects.semantic.delivery.outbox.id.OutboxId

public class OutboxConflictException(id: OutboxId) : IllegalStateException("Outbox $id already exists")
