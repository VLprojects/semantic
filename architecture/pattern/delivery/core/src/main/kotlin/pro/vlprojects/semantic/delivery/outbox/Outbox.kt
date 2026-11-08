package pro.vlprojects.semantic.delivery.outbox

public interface Outbox {

    public fun schedule(
        payload: Any,
        key: String,
        context: Map<String, String> = emptyMap(),
        idempotencyKey: String? = null,
    )

    public fun schedule(payload: Any)
}
