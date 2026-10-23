package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.support.abbreviated
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.EventId
import java.time.Instant
import java.util.Objects

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

    override fun hashCode(): Int = Objects.hash(eventId, name, occurredAt)

    override fun toString(): String = "[id: ${eventId.toString().abbreviated()}, name: $name, occurredAt: $occurredAt]"

    public companion object Factory {

        public fun of(name: String, occurredAt: Instant = Instant.now()): BaseEventHeader = BaseEventHeader(
            eventId = UuidEventId.random(),
            name = name,
            occurredAt = occurredAt,
        )
    }
}
