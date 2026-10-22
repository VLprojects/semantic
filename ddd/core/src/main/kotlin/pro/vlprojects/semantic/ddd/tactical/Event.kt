package pro.vlprojects.semantic.ddd.tactical

/**
 * Something that happened to an aggregate of type [A], identified by [I], told to whoever cares, in this bounded
 * context or another.
 *
 * An event is its [header], the [aggregate] it happened to and the fields of its own that say what happened. The
 * aggregate type is known to the compiler only: an aggregate raises its own events and no other, and nothing of the
 * aggregate but its revision travels with the event. Whatever else a receiver needs, the event carries in fields of
 * its own.
 *
 * ```kotlin
 * sealed interface CartEvent : Event<Cart, CartId>
 * ```
 */
public interface Event<A : AggregateRoot<A, I>, out I : Identifier> {
    public val header: EventHeader

    public val aggregate: AggregateRevision<I>
}
