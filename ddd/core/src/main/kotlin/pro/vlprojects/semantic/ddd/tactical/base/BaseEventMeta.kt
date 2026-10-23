package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.support.abbreviated
import pro.vlprojects.semantic.ddd.tactical.EventId
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import java.time.Instant
import java.util.Objects

public class BaseEventMeta(
    override val eventId: EventId = UuidEventId.random(),
    override val occurredAt: Instant = Instant.now(),
) : EventMeta {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is BaseEventMeta -> false
        else -> eventId == other.eventId && occurredAt == other.occurredAt
    }

    override fun hashCode(): Int = Objects.hash(eventId, occurredAt)

    override fun toString(): String = "[id: ${eventId.toString().abbreviated()}, occurredAt: $occurredAt]"
}
