package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.EventId
import java.time.Instant

public class BaseEventHeader(
    override val eventId: EventId,
    override val name: String,
    override val occurredAt: Instant,
) : EventHeader {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is BaseEventHeader -> false
        else -> eventId == other.eventId && name == other.name && occurredAt == other.occurredAt
    }

    override fun hashCode(): Int = eventId.hashCode()

    override fun toString(): String = "$name[$eventId at $occurredAt]"
}
