package pro.vlprojects.semantic.ddd.model

import pro.vlprojects.semantic.ddd.event.DomainEvent
import pro.vlprojects.semantic.ddd.identity.Identifier
import pro.vlprojects.semantic.ddd.rule.BusinessRule
import pro.vlprojects.semantic.ddd.rule.check

/**
 * Aggregate root that records its events and checks its invariants after every change:
 *
 * ```kotlin
 * class Pipeline(override val identifier: PipelineId) : BaseAggregateRoot<PipelineId, PipelineEvent>() {
 *     private val nodes = mutableListOf<Node>()
 *
 *     override val invariants = listOf(
 *         rule<Pipeline>({ nodes.size <= 100 }) { "Pipeline $identifier has more than 100 nodes" },
 *     )
 *
 *     fun add(node: Node) = change {
 *         nodes += node
 *         record(NodeAdded(identifier, node.identifier))
 *     }
 * }
 * ```
 */
public abstract class BaseAggregateRoot<I : Identifier, E : DomainEvent<I>> : AggregateRoot<I, E> {
    private val recorded = mutableListOf<E>()

    /**
     * Rules that hold for the root after every [change]. A root declares rules over its own type, and the list is
     * typed by `Nothing` so that it takes them: a `BusinessRule<Pipeline>` is a `BusinessRule<Nothing>`.
     */
    protected open val invariants: List<BusinessRule<Nothing>>
        get() = emptyList()

    final override fun peekEvents(): List<E> = recorded.toList()

    final override fun pullEvents(): List<E> = recorded.toList().also { recorded.clear() }

    /** Records an event to be published when the root is saved. */
    protected fun record(event: E) {
        recorded += event
    }

    /**
     * Applies [block] and checks the [invariants]. A change either completes with its events or leaves none: when
     * [block] throws or an invariant breaks, the events recorded in [block] are dropped and the exception goes on, a
     * broken invariant as [pro.vlprojects.semantic.ddd.error.BusinessRuleException].
     *
     * The state [block] changed is not restored. In a service the transaction rolls back on the exception; a root
     * caught outside of one is not to be used further.
     */
    protected fun <A : BaseAggregateRoot<I, E>> A.change(block: A.() -> Unit): A {
        val recordedBefore = recorded.size
        try {
            block()
            // The invariants of a root are rules over its own type, so they accept the root itself.
            @Suppress("UNCHECKED_CAST")
            (invariants as List<BusinessRule<A>>).check(this)
        } catch (failure: Throwable) {
            repeat(recorded.size - recordedBefore) { recorded.removeAt(recorded.lastIndex) }
            throw failure
        }
        return this
    }
}
