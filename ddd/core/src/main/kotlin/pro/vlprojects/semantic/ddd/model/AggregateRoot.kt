package pro.vlprojects.semantic.ddd.model

import pro.vlprojects.semantic.ddd.event.DomainEvent
import pro.vlprojects.semantic.ddd.identity.Identifier

/**
 * The only way into an aggregate and the boundary of its transaction. Changes leave the aggregate as domain events,
 * recorded while it changes and published once it is saved.
 */
public interface AggregateRoot<out I : Identifier, out E : DomainEvent<I>> : Entity<I> {
    /** Events recorded since the root was created or loaded, oldest first. Leaves them in place. */
    public fun peekEvents(): List<E>

    /** Takes the recorded events and clears them in one step, so that none is cleared before it is published. */
    public fun pullEvents(): List<E>
}
