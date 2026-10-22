package pro.vlprojects.semantic.ddd.tactical

import java.time.Instant

/**
 * Something that happened to an aggregate of type [A], identified by [I], told to whoever cares, in this bounded
 * context or another.
 *
 * An event carries what makes it a fact: which one it is, the [AggregateRevision] it happened at and when. All three
 * are set once, when the event is raised, and are constructor parameters of the implementation, so that an event read
 * back from storage keeps its own rather than getting new ones. Its type is not a property: it is the same for every
 * instance and is set by [EventType].
 *
 * The aggregate type is known to the compiler only: an aggregate raises its own events and no other, and nothing of
 * the aggregate but its revision travels with the event. Whatever else a receiver needs, the event carries in fields
 * of its own.
 *
 * ```kotlin
 * sealed interface CartEvent : Event<Cart, CartId>
 * ```
 */
public interface Event<A : AggregateRoot<A, I>, out I : Identifier> {
    public val eventId: EventId

    public val aggregate: AggregateRevision<I>

    public val occurredAt: Instant
}
