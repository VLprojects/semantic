package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.EventId
import java.util.UUID

public class BaseEventId(value: UUID) : AbstractId<UUID>(value), EventId {

    public companion object Factory {

        public fun new(): BaseEventId = UUID.randomUUID().let(::BaseEventId)
    }
}
