package pro.vlprojects.semantic.ddd.model

import pro.vlprojects.semantic.ddd.event.DomainEvent
import pro.vlprojects.semantic.ddd.id.Identifier

/**
 * The only way into an aggregate and the boundary of its transaction. Changes leave the aggregate as events of type
 * [E], recorded while it changes and released once it is saved:
 *
 * ```kotlin
 * sealed interface CartEvent : DomainEvent
 *
 * class Cart(override val identifier: CartId) : AggregateRoot<CartId, CartEvent>
 * ```
 */
public interface AggregateRoot<out I : Identifier, out E : DomainEvent> : Entity<I> {
    /** Events recorded and not yet released, in the order recorded. Reading them leaves them in place. */
    public val recordedEvents: Collection<E>

    /**
     * Hands the recorded events out, in the order recorded, and forgets them in one step, so that none is forgotten
     * before it is handed out.
     */
    public fun releaseEvents(): Collection<E>
}
