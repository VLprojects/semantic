package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.id.OutboxId

public class OutboxAlreadyExistsException(id: OutboxId) : IllegalStateException("Outbox $id already exists")
