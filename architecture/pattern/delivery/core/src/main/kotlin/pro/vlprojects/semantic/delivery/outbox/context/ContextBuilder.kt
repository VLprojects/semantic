package pro.vlprojects.semantic.delivery.outbox.context

public class ContextBuilder {

    private var target: String? = null
    private var orderingKey: String? = null
    private var idempotencyKey: String? = null
    private val attributes = mutableMapOf<String, String>()

    public fun target(target: String): ContextBuilder = apply { this.target = target }

    public fun orderingKey(orderingKey: String): ContextBuilder = apply { this.orderingKey = orderingKey }

    public fun idempotencyKey(idempotencyKey: String): ContextBuilder = apply { this.idempotencyKey = idempotencyKey }

    public fun attributes(attributes: Map<String, String>): ContextBuilder = apply { this.attributes += attributes }

    public fun build(): Context = Context.of(
        target = checkNotNull(target) { "No target was determined for the outbox" },
        orderingKey = orderingKey,
        idempotencyKey = idempotencyKey,
        attributes = attributes.toMap(),
    )
}
