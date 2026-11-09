package pro.vlprojects.semantic.delivery.outbox

import java.util.UUID

public class UuidOutboxIdGenerator : OutboxIdGenerator {

    override fun generate(): OutboxId = UUID
        .randomUUID()
        .toString()
        .let(OutboxId.Factory::of)
}
