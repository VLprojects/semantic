package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.EventId
import java.time.Instant

/** An [EventHeader] that holds what it is given. */
public class SimpleEventHeader(
    override val eventId: EventId,
    override val name: String,
    override val occurredAt: Instant,
) : EventHeader {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleEventHeader) return false
        return eventId == other.eventId && name == other.name && occurredAt == other.occurredAt
    }

    override fun hashCode(): Int = eventId.hashCode()

    override fun toString(): String = "$name[$eventId at $occurredAt]"
}
