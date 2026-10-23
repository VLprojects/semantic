package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.support.abbreviated
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier
import java.time.Instant

public abstract class AbstractEvent<A : AggregateRoot<A, I>, out I : Identifier>(
    final override val header: EventHeader,
    final override val aggregateRevision: AggregateRevision<I>,
) : Event<A, I> {

    protected constructor(name: String, aggregate: A, occurredAt: Instant = Instant.now()) :
        this(BaseEventHeader.of(name, occurredAt), BaseRevision.of(aggregate))

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Event<*, *> -> false
        else -> header.eventId == other.header.eventId
    }

    override fun hashCode(): Int = header.eventId.hashCode()

    override fun toString(): String =
        "${this::class.simpleName} [eventId: ${header.eventId.toString().abbreviated()}, aggregate: $aggregateRevision]"
}
