package pro.vlprojects.semantic.ddd.tactical

/**
 * Identifier of an [Event], unique per event, so that a receiver can drop a second delivery of the same one. Its value
 * is up to the implementation:
 *
 * ```kotlin
 * @JvmInline
 * value class CartEventId(override val value: UUID) : EventId, Id<UUID>
 * ```
 */
public interface EventId : Identifier
