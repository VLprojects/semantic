package pro.vlprojects.semantic.ddd.tactical

import java.time.Instant

public interface EventMeta {

    public val eventId: EventId

    public val occurredAt: Instant
}
