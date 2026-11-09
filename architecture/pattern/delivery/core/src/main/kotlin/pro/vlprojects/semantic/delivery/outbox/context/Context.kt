package pro.vlprojects.semantic.delivery.outbox.context

import java.util.UUID

@ConsistentCopyVisibility
public data class Context private constructor(
    public val target: String,
    public val orderingKey: String,
    public val idempotencyKey: String,
    public val attributes: Map<String, String>,
) {

    public fun hasTarget(): Boolean = target != NO_TARGET

    public fun withTarget(target: String): Context = copy(target = target)

    public fun withOrderingKey(orderingKey: String): Context = copy(orderingKey = orderingKey)

    public fun withIdempotencyKey(idempotencyKey: String): Context = copy(idempotencyKey = idempotencyKey)

    public fun withDefaultAttributes(defaults: Map<String, String>): Context = copy(attributes = defaults + attributes)

    public companion object Factory {

        private const val NO_TARGET = ""

        public fun of(
            target: String = NO_TARGET,
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
