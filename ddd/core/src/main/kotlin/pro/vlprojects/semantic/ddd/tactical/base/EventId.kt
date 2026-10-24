package pro.vlprojects.semantic.ddd.tactical.base

import java.util.UUID

public class EventId(value: UUID) : AbstractId<UUID>(value) {

    public companion object Factory {

        public fun new(): EventId = UUID.randomUUID().let(::EventId)
    }
}
