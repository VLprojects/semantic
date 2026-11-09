package pro.vlprojects.semantic.delivery.outbox.context

public data class Context(
    public val deliverer: String,
    public val orderingKey: String? = null,
    public val idempotencyKey: String? = null,
    public val attributes: Map<String, String> = emptyMap(),
)
