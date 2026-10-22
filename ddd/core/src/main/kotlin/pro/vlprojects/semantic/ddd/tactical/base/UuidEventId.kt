package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.EventId
import java.util.UUID

/** An [EventId] of a random [UUID], unique across all events without asking anyone. */
@JvmInline
public value class UuidEventId(
    public val value: UUID,
) : EventId {
    override fun toString(): String = value.toString()

    public companion object {
        /** A new id, different from every other one. */
        public fun random(): UuidEventId = UuidEventId(UUID.randomUUID())
    }
}
