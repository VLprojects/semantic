package pro.vlprojects.semantic.ddd.tactical

import java.time.Instant
import java.util.UUID

/**
 * Something that happened in the domain, told to whoever cares, in this bounded context or another.
 *
 * An event carries what makes it a fact: which one it is and when it happened. Both are set once, when the event is
 * raised, and are constructor parameters of the implementation, so that an event read back from storage keeps its
 * own rather than getting new ones. Its type is not a property: it is the same for every instance and is set by
 * [EventType].
 *
 * An event is not typed by its aggregate, so it can live in a module of its own, apart from the model. Whatever a
 * receiver needs, the identifier of the aggregate included, the event carries in fields of its own.
 */
public interface Event {
    /** Unique per event, so that a receiver can drop a second delivery of the same one. */
    public val eventId: UUID

    public val occurredAt: Instant
}
