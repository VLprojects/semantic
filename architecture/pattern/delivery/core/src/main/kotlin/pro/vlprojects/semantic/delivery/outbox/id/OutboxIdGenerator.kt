package pro.vlprojects.semantic.delivery.outbox.id

public fun interface OutboxIdGenerator {

    public fun generate(): OutboxId
}
