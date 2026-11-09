package pro.vlprojects.semantic.delivery.outbox

public data class OutboxMetadata(
    public val orderingKey: String? = null,
    public val idempotencyKey: String? = null,
    public val context: Map<String, String> = emptyMap(),
)
