package pro.vlprojects.semantic.ddd.tactical

/**
 * Identity of an entity. Each entity type gets an identifier type of its own, so an order id cannot be passed where a
 * customer id is expected:
 *
 * ```kotlin
 * @JvmInline
 * value class OrderId(val value: UUID) : Identifier
 * ```
 */
public interface Identifier
