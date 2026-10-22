package pro.vlprojects.semantic.ddd.tactical

/**
 * Identifier of an [Event], unique across all events, so that a receiver can drop a second delivery of the same one.
 * One implementation usually serves the whole application:
 *
 * ```kotlin
 * @JvmInline
 * value class UuidEventId(val value: UUID) : EventId
 * ```
 */
public interface EventId : Identifier
