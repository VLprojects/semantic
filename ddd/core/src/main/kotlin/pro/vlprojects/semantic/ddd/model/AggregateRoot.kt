package pro.vlprojects.semantic.ddd.model

import pro.vlprojects.semantic.ddd.event.DomainEvent
import pro.vlprojects.semantic.ddd.id.Identifier

/**
 * The only way into an aggregate and the boundary of its transaction. Changes leave the aggregate as domain events,
 * recorded while it changes and released once it is saved.
 *
 * The events are not typed by the aggregate: only the aggregate itself records them, so only its own events get in,
 * and whoever releases them, an adapter publishing them, does not need their type.
 */
public interface AggregateRoot<out I : Identifier> : Entity<I> {
    /** Events recorded and not yet released, in the order recorded. Reading them leaves them in place. */
    public val recordedEvents: Collection<DomainEvent>

    /**
     * Hands the recorded events out, in the order recorded, and forgets them in one step, so that none is forgotten
     * before it is handed out.
     */
    public fun releaseEvents(): Collection<DomainEvent>
}
