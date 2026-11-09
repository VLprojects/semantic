package pro.vlprojects.semantic.delivery.outbox

public fun interface PayloadSerializer {

    public fun serialize(content: Any): Payload
}
