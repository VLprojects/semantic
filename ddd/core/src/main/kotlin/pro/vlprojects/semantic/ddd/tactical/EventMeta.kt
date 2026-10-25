package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.tactical.base.EventId
import pro.vlprojects.semantic.format.abbreviated
import java.time.Instant

public class EventMeta(
    public val eventId: Identifier,
    public val eventName: String,
    public val eventOccurredAt: Instant,
) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is EventMeta -> false
        else -> eventId == other.eventId
    }

    override fun hashCode(): Int = eventId.hashCode()

    override fun toString(): String =
        "[eventId: ${eventId.toString().abbreviated()}, eventName: $eventName, eventOccurredAt: $eventOccurredAt]"

    public companion object Factory {

        public inline fun <reified E : Event<*, *>> of(
            eventId: Identifier = EventId.new(),
            eventOccurredAt: Instant = Instant.now(),
        ): EventMeta = EventMeta(
            eventId = eventId,
            eventName = E::class.typeName,
            eventOccurredAt = eventOccurredAt,
        )
    }
}
