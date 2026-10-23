package pro.vlprojects.semantic.ddd.tactical

import java.time.Instant

public interface EventHeader {
    public val eventId: EventId

    public val name: String

    public val occurredAt: Instant
}
