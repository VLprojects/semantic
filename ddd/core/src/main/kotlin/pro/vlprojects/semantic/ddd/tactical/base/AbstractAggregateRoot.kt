package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.dsl.check
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier
import java.time.Clock

/**
 * An [AggregateRoot] that records its events and keeps its invariants.
 *
 * A change goes through [change]: the block changes the state and records events, and the invariants are checked once
 * it is done, so the aggregate is never left broken. The events stay inside; a persistence module hands them out when
 * the aggregate is saved, through [recordedEvents] and [clearEvents].
 *
 * The version is left to the subclass, as storage moves it forward: the JPA module maps it.
 *
 * ```kotlin
 * class Cart(override val identifier: CartId, override val version: Long = 0) : AbstractAggregateRoot<Cart, CartId>() {
 *     override val aggregateName: String get() = "cart"
 *     override val invariants: List<BusinessRule<Cart>> get() = INVARIANTS
 *
 *     fun add(sku: Sku): Cart = change {
 *         lines += Line(sku)
 *         record(ItemAdded(header("cart.item-added"), revision(), sku))
 *     }
 * }
 * ```
 */
public abstract class AbstractAggregateRoot<A : AbstractAggregateRoot<A, I>, I : Identifier> :
    AbstractEntity<A, I>(),
    AggregateRoot<A, I> {
    private val events = mutableListOf<Event<A, I>>()

    /**
     * The kind of the aggregate in its events, such as `cart`: kept when the class is renamed. A getter rather than a
     * field, so that it is not stored with the state.
     */
    protected abstract val aggregateName: String

    /** The rules the aggregate keeps after every [change]; none by default. */
    protected open val invariants: Iterable<BusinessRule<A>>
        get() = emptyList()

    /** The clock events are dated by; UTC by default, a fixed one in tests. */
    protected open val clock: Clock
        get() = Clock.systemUTC()

    @Suppress("UNCHECKED_CAST")
    private val self: A
        get() = this as A

    /** Runs [block] on the aggregate, then checks the [invariants], so that a change cannot break them. */
    protected fun change(block: A.() -> Unit): A {
        self.block()
        invariants.check(self)
        return self
    }

    /** Records [event] to be handed out once the aggregate is saved. */
    protected fun record(event: Event<A, I>) {
        events += event
    }

    /** A header for a new event named [eventName]: a random id and the time of the [clock]. */
    protected fun header(eventName: String): EventHeader =
        SimpleEventHeader(UuidEventId.random(), eventName, clock.instant())

    /** The revision the aggregate is at, for a new event. */
    protected fun revision(): AggregateRevision<I> = SimpleAggregateRevision(aggregateName, identifier, version)

    /** The events recorded since the last [clearEvents], in the order recorded. */
    protected fun recordedEvents(): List<Event<A, I>> = events.toList()

    /** Forgets the recorded events once they are handed out. */
    protected fun clearEvents() {
        events.clear()
    }
}
