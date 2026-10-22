package pro.vlprojects.semantic.ddd.event

import pro.vlprojects.semantic.ddd.identity.Identifier
import java.time.Instant
import java.util.UUID

/**
 * Something that happened to an aggregate, told to whoever cares, in this bounded context or another.
 *
 * An event refers to its aggregate by identifier, not by the aggregate itself. A receiver in another context cannot even
 * name the root type, and the event travels as JSON, while the aggregate is lazy collections and a state that has
 * moved on by the time the event arrives. Whatever else the receiver needs, the event carries in fields of its own.
 */
public interface DomainEvent<out I : Identifier> {
    /** Unique per event, so that a receiver can drop a second delivery of the same one. */
    public val eventId: UUID

    /** Stable name of the event type. Not the class name: renaming a class must not break the receivers. */
    public val eventType: String

    public val occurredAt: Instant

    public val aggregate: I
}
