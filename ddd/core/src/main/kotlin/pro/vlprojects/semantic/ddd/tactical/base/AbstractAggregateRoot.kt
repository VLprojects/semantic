package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier

/**
 * An [AggregateRoot] that registers its events and, as an [AbstractEntity], keeps its invariants.
 *
 * A [change] registers the events of what it did. The events stay inside; a persistence module hands them out when
 * the aggregate is saved, through [events] and [clearEvents].
 *
 * The version is left to the subclass, as storage moves it forward: the JPA module maps it.
 *
 * ```kotlin
 * class Cart(override val identifier: CartId, override val version: Long = 0) : AbstractAggregateRoot<Cart, CartId>() {
 *     override val aggregateName: String get() = "cart"
 *
 *     fun add(sku: Sku): Cart = change {
 *         lines += Line(sku)
 *         registerEvent(ItemAdded(SimpleEventHeader(UuidEventId.random(), "cart.item-added", now()), revision, sku))
 *     }
 * }
 * ```
 */
public abstract class AbstractAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractEntity<A, I>(),
    AggregateRoot<A, I> {

    private val registered = mutableListOf<Event<A, I>>()

    /**
     * The kind of the aggregate in its events, such as `cart`: kept when the class is renamed. A getter rather than a
     * field, so that it is not stored with the state.
     */
    protected abstract val aggregateName: String

    /** The revision the aggregate is at, for a new event. */
    protected val revision: AggregateRevision<I>
        get() = SimpleAggregateRevision(aggregateName, identifier, version)

    /** The events registered since the last [clearEvents], in the order registered. */
    protected val events: List<Event<A, I>>
        get() = registered.toList()

    /** Registers [event] to be handed out once the aggregate is saved. */
    protected fun registerEvent(event: Event<A, I>): AbstractAggregateRoot<A, I> = apply { registered.add(event) }

    /** Forgets the registered events once they are handed out. */
    protected fun clearEvents(): AbstractAggregateRoot<A, I> = apply { registered.clear() }
}
