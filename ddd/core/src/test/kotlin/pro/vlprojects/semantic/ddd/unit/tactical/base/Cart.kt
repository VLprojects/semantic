package pro.vlprojects.semantic.ddd.unit.tactical.base

import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import java.time.Instant

@JvmInline
value class CartId(
    val value: Long,
) : Identifier

class ItemAdded(
    header: EventHeader,
    aggregate: AggregateRevision<CartId>,
    val sku: String,
) : AbstractEvent<Cart, CartId>(header, aggregate)

class Cart(
    override val identifier: CartId,
    override val version: Long = 0,
) : AbstractAggregateRoot<Cart, CartId>() {
    val items = mutableListOf<String>()

    override val aggregateName: String get() = "cart"

    override val invariants: List<BusinessRule<Cart>> get() = INVARIANTS

    fun add(sku: String): Cart =
        change {
            items += sku
            record(ItemAdded(header("cart.item-added", NOW), revision(), sku))
        }

    fun events(): List<Event<Cart, CartId>> = recordedEvents()

    fun saved() = clearEvents()

    companion object {
        val NOW: Instant = Instant.parse("2026-10-23T10:00:00Z")
        val INVARIANTS = listOf(rule<Cart>({ items.size <= 2 }) { "Cart $identifier holds more than 2 items" })
    }
}
