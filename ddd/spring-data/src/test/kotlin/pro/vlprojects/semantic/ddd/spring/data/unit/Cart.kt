package pro.vlprojects.semantic.ddd.spring.data.unit

import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.Revision
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision

class CartId(
    value: Long,
) : AbstractId<Long>(value)

class ItemAdded(
    meta: EventMeta,
    aggregateRevision: Revision<CartId>,
    val sku: String,
) : AbstractEvent<Cart, CartId>(meta, aggregateRevision) {

    companion object Factory {

        fun of(cart: Cart, sku: String) = ItemAdded(EventMeta.of<ItemAdded>(), BaseRevision.of(cart), sku)
    }
}

class Cart(
    identifier: CartId,
    version: Long = 0,
) : AbstractSpringDataAggregateRoot<Cart, CartId>() {

    init {
        this.identifier = identifier
        this.version = version
    }

    val items = mutableListOf<String>()

    override val invariants get() = INVARIANTS

    fun add(sku: String) = change {
        items += sku
        register(ItemAdded.of(this, sku))
    }

    companion object {

        val INVARIANTS = listOf(rule<Cart>({ items.size <= 2 }) { "Cart $identifier holds more than 2 items" })
    }
}
