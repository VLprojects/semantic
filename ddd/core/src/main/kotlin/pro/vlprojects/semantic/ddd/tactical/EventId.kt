package pro.vlprojects.semantic.ddd.tactical

import java.util.UUID

/** Identifier of an [Event], unique per event, so that a receiver can drop a second delivery of the same one. */
@JvmInline
public value class EventId(
    override val value: UUID,
) : Id<UUID>
