package pro.vlprojects.semantic.ddd.tactical

import java.time.Instant

/**
 * Something that happened to an aggregate of type [A], told to whoever cares, in this bounded context or another.
 *
 * An event carries what makes it a fact: which one it is and when it happened. Both are set once, when the event is
 * raised, and are constructor parameters of the implementation, so that an event read back from storage keeps its
 * own rather than getting new ones. Its type is not a property: it is the same for every instance and is set by
 * [EventType].
 *
 * The aggregate type is known to the compiler only: an aggregate releases its own events and no other, and nothing
 * of the aggregate travels with the event. Whatever a receiver needs, the identifier of the aggregate included, the
 * event carries in fields of its own.
 *
 * ```kotlin
 * sealed interface CartEvent : Event<Cart>
 * ```
 */
public interface Event<A : AggregateRoot<A, *>> : Identifiable<EventId> {
    public val occurredAt: Instant
}
