package pro.vlprojects.semantic.delivery.outbox

public fun interface OutboxIdGenerator {

    public fun generate(): OutboxId
}
