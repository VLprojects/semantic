package pro.vlprojects.semantic.ddd.tactical

import java.util.UUID

public class UuidEventId(
    public val value: UUID,
) : EventId {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is UuidEventId -> false
        else -> value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()

    public companion object Factory {

        public fun random(): UuidEventId = UUID.randomUUID().let(::UuidEventId)
    }
}
