package pro.vlprojects.semantic.ddd.event

import pro.vlprojects.semantic.ddd.identity.Identifier
import java.time.Instant
import java.util.UUID

/**
 * Domain event with a random id and the current time, equal to another event with the same id. Both can be passed
 * in: a test fixes them, an event read back from storage keeps its own.
 *
 * ```kotlin
 * class NodeAdded(override val aggregate: PipelineId, val node: NodeId) : BaseDomainEvent<PipelineId>() {
 *     override val eventType: String get() = "pipeline.node-added"
 * }
 * ```
 */
public abstract class BaseDomainEvent<out I : Identifier>(
    final override val eventId: UUID = UUID.randomUUID(),
    final override val occurredAt: Instant = Instant.now(),
) : DomainEvent<I> {
    final override fun equals(other: Any?): Boolean = other is DomainEvent<*> && other.eventId == eventId

    final override fun hashCode(): Int = eventId.hashCode()

    override fun toString(): String = "$eventType[eventId: $eventId]"
}
