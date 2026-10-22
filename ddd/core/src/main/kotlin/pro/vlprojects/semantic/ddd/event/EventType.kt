package pro.vlprojects.semantic.ddd.event

/**
 * Stable name of a [DomainEvent] type, by which receivers tell events apart:
 *
 * ```kotlin
 * @EventType("cart.item-added")
 * data class ItemAdded(val cart: CartId, val item: String) : CartEvent
 * ```
 *
 * Not the class name: renaming a class must not break the receivers. An adapter refuses to publish an event without
 * this annotation.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
public annotation class EventType(
    val value: String,
)
