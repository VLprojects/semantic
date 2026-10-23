package pro.vlprojects.semantic.ddd.unit.tactical.base

import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import pro.vlprojects.semantic.ddd.tactical.base.SimpleEventHeader
import pro.vlprojects.semantic.ddd.tactical.base.UuidEventId
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

    override val invariants get() = INVARIANTS

    fun add(sku: String) = change {
        items += sku
        SimpleEventHeader(UuidEventId.random(), "cart.item-added", NOW)
            .let { ItemAdded(it, revision, sku) }
            .let { register(it) }
    }

    fun released() = getEvents()

    fun saved() = clearEvents()

    companion object {
        val NOW = Instant.parse("2026-10-23T10:00:00Z")
        val INVARIANTS = listOf(rule<Cart>({ items.size <= 2 }) { "Cart $identifier holds more than 2 items" })
    }
}

class WishList(
    override val identifier: CartId,
    override val version: Long = 0,
) : AbstractAggregateRoot<WishList, CartId>() {
    val name get() = aggregateName
}

class RenamedCart(
    override val identifier: CartId,
    override val version: Long = 0,
) : AbstractAggregateRoot<RenamedCart, CartId>() {
    override val aggregateName get() = "cart"

    val name get() = aggregateName
}
