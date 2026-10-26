package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.Revision
import pro.vlprojects.semantic.format.abbreviation.abbreviated

public abstract class AbstractEvent<A : AggregateRoot<A, I>, out I : Identifier>(
    final override val meta: EventMeta,
    final override val aggregateRevision: Revision<I>,
) : Event<A, I> {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Event<*, *> -> false
        else -> meta.eventId == other.meta.eventId
    }

    override fun hashCode(): Int = meta.eventId.hashCode()

    override fun toString(): String =
        "${this::class.simpleName} [eventId: ${meta.eventId.toString().abbreviated()}, aggregate: $aggregateRevision]"
}
