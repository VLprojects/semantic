package pro.vlprojects.semantic.delivery.outbox.context

import java.util.UUID

@ConsistentCopyVisibility
public data class Context private constructor(
    public val target: String,
    public val orderingKey: String,
    public val idempotencyKey: String,
    public val attributes: Map<String, String>,
) {

    internal fun withDefaultAttributes(defaults: Map<String, String>) = copy(attributes = defaults + attributes)

    public companion object Factory {

        public fun of(
            target: String,
            orderingKey: String? = null,
            idempotencyKey: String? = null,
            attributes: Map<String, String> = emptyMap(),
        ): Context = Context(
            target = target,
            orderingKey = orderingKey ?: generateKey(),
            idempotencyKey = idempotencyKey ?: generateKey(),
            attributes = attributes,
        )

        private fun generateKey() = UUID
            .randomUUID()
            .toString()
    }
}
