package pro.vlprojects.semantic.delivery.outbox.context

@ConsistentCopyVisibility
public data class Context private constructor(
    public val orderingKey: String?,
    public val idempotencyKey: String?,
    public val attributes: Map<String, String>,
) {

    public class Builder internal constructor() {

        private var orderingKey: String? = null
        private var idempotencyKey: String? = null
        private val attributes = mutableMapOf<String, String>()

        public fun setOrderingKey(orderingKey: String): Builder = apply { this.orderingKey = orderingKey }

        public fun setIdempotencyKey(idempotencyKey: String): Builder = apply { this.idempotencyKey = idempotencyKey }

        public fun addAllAttributes(attributes: Map<String, String>): Builder = apply { this.attributes += attributes }

        public fun build(): Context = of(
            orderingKey = orderingKey,
            idempotencyKey = idempotencyKey,
            attributes = attributes.toMap(),
        )
    }

    public companion object Factory {

        public fun builder(): Builder = Builder()

        public fun of(
            orderingKey: String? = null,
            idempotencyKey: String? = null,
            attributes: Map<String, String> = emptyMap(),
        ): Context = Context(
            orderingKey = orderingKey,
            idempotencyKey = idempotencyKey,
            attributes = attributes,
        )
    }
}
