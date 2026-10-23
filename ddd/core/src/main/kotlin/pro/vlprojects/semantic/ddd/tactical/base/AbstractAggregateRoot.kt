package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier

/**
 * An [AggregateRoot] that registers its events and, as an [AbstractEntity], keeps its invariants.
 *
 * A [change] registers the events of what it did. The events stay inside; a persistence module hands them out when
 * the aggregate is saved, through [getEvents] and [clearEvents].
 *
 * The version is left to the subclass, as storage moves it forward: the JPA module maps it.
 *
 * ```kotlin
 * class Cart(override val identifier: CartId, override val version: Long = 0) : AbstractAggregateRoot<Cart, CartId>() {
 *     fun add(sku: Sku): Cart = change {
 *         lines += Line(sku)
 *         register(ItemAdded(SimpleEventHeader(UuidEventId.random(), "cart.item-added", now()), revision, sku))
 *     }
 * }
 * ```
 */
public abstract class AbstractAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractEntity<A, I>(),
    AggregateRoot<A, I> {

    private val events = mutableListOf<Event<A, I>>()

    /**
     * The kind of the aggregate in its events: the class name in kebab case, `shopping-cart` for `ShoppingCart`.
     *
     * Receivers route by it, so renaming the class changes it for them. Override it with the old name when the class
     * is renamed, as a getter rather than a field, so that it is not stored with the state.
     */
    protected open val aggregateName: String
        get() {
            val name = checkNotNull(classOf(this).simpleName) { "An aggregate without a class name must name itself" }
            return name.replace(WORD_BOUNDARY, "$1$3-$2$4").lowercase()
        }

    /** The revision the aggregate is at: its name, identifier and version, as a new event names it. */
    public val revision: AggregateRevision<I>
        get() = SimpleAggregateRevision(aggregateName, identifier, version)

    /** The events registered since the last [clearEvents], in the order registered. */
    protected fun getEvents(): List<Event<A, I>> = events.toList()

    /** Registers [event] to be handed out once the aggregate is saved. */
    protected fun register(event: Event<A, I>): AbstractAggregateRoot<A, I> = apply { events.add(event) }

    /** Forgets the registered events once they are handed out. */
    protected fun clearEvents(): AbstractAggregateRoot<A, I> = apply { events.clear() }

    private companion object {
        /** Where a word starts in `CamelCase`, including after an acronym: `HTTPRequest` is `http-request`. */
        val WORD_BOUNDARY = Regex("([a-z0-9])([A-Z])|([A-Z])([A-Z][a-z])")
    }
}
