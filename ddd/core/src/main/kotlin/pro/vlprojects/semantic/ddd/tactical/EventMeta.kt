package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.support.abbreviated
import java.time.Instant
import java.util.Objects

public class EventMeta(
    public val eventId: EventId,
    public val eventName: String,
    public val occurredAt: Instant,
) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is EventMeta -> false
        else -> eventId == other.eventId && eventName == other.eventName && occurredAt == other.occurredAt
    }

    override fun hashCode(): Int = Objects.hash(eventId, eventName, occurredAt)

    override fun toString(): String =
        "[eventId: ${eventId.toString().abbreviated()}, eventName: $eventName, occurredAt: $occurredAt]"

    public companion object Factory {

        public inline fun <reified E : Event<*, *>> of(
            eventId: EventId = UuidEventId.random(),
            occurredAt: Instant = Instant.now(),
        ): EventMeta = EventMeta(
            eventId = eventId,
            eventName = E::class.typeName,
            occurredAt = occurredAt,
        )
    }
}
