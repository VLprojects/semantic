package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.support.abbreviated
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.typeName
import java.time.Instant

public abstract class AbstractEvent<A : AggregateRoot<A, I>, out I : Identifier>(
    final override val header: EventHeader,
    final override val aggregateRevision: AggregateRevision<I>,
) : Event<A, I> {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Event<*, *> -> false
        else -> header.eventId == other.header.eventId
    }

    override fun hashCode(): Int = header.eventId.hashCode()

    override fun toString(): String =
        "${this::class.simpleName} [eventId: ${header.eventId.toString().abbreviated()}, aggregate: $aggregateRevision]"

    public companion object Factory {

        public inline fun <reified E : AbstractEvent<A, I>, A : AggregateRoot<A, I>, I : Identifier> of(
            aggregate: A,
            occurredAt: Instant = Instant.now(),
            create: (EventHeader, AggregateRevision<I>) -> E,
        ): E = BaseEventHeader
            .of(E::class.typeName, occurredAt)
            .let { create(it, BaseRevision.of(aggregate)) }
    }
}
