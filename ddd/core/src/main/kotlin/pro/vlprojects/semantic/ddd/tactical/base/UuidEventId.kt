package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.EventId
import java.util.UUID

@JvmInline
public value class UuidEventId(
    public val value: UUID,
) : EventId {
    override fun toString(): String = value.toString()

    public companion object {

        public fun random(): UuidEventId = UUID.randomUUID().let(::UuidEventId)
    }
}
