package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier

/**
 * An [Event] equal to another with the same id: a second delivery of an event is the same event.
 *
 * ```kotlin
 * class ItemAdded(header: EventHeader, aggregate: AggregateRevision<CartId>, val sku: Sku) :
 *     AbstractEvent<Cart, CartId>(header, aggregate), CartEvent
 * ```
 */
public abstract class AbstractEvent<A : AggregateRoot<A, I>, out I : Identifier>(
    final override val header: EventHeader,
    final override val aggregate: AggregateRevision<I>,
) : Event<A, I> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Event<*, *>) return false
        return header.eventId == other.header.eventId
    }

    override fun hashCode(): Int = header.eventId.hashCode()

    override fun toString(): String = "${header.name}[${header.eventId}]"
}
