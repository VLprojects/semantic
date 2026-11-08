package pro.vlprojects.semantic.delivery.outbox

public interface OutboxPayloadSerializer {

    public fun serialize(payload: Any): String

    public fun <T : Any> deserialize(text: String, type: Class<T>): T
}
