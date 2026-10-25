package pro.vlprojects.semantic.ddd.unit.tactical.base

import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.Revision
import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEntity
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.time.Instant

class CartId(
    value: Long,
) : AbstractId<Long>(value)

class ItemAdded(
    meta: EventMeta,
    aggregateRevision: Revision<CartId>,
    val sku: String,
) : AbstractEvent<Cart, CartId>(meta, aggregateRevision) {

    companion object Factory {

        fun of(cart: Cart, sku: String, meta: EventMeta = EventMeta.of<ItemAdded>()) =
            ItemAdded(meta, cart.revision, sku)
    }
}

class Cart(
    identifier: CartId,
    version: Long = 0,
) : AbstractAggregateRoot<Cart, CartId>() {

    init {
        this.identifier = identifier
        this.version = version
    }

    val items = mutableListOf<String>()

    override val invariants get() = INVARIANTS

    fun add(sku: String) = change {
        items += sku
        register(ItemAdded.of(this, sku, EventMeta.of<ItemAdded>(eventOccurredAt = NOW)))
    }

    fun released() = events

    fun saved() = clearEvents()

    companion object {

        val NOW = Instant.parse("2026-10-23T10:00:00Z")
        val INVARIANTS = listOf(rule<Cart>({ items.size <= 2 }) { "Cart $identifier holds more than 2 items" })
    }
}

class Line(
    identifier: CartId,
) : AbstractEntity<Line, CartId>() {

    init {
        this.identifier = identifier
    }

    fun versioned(version: Long) = apply { assignVersion(version) }
}

class WishList(
    identifier: CartId,
) : AbstractAggregateRoot<WishList, CartId>() {

    init {
        this.identifier = identifier
    }
}
