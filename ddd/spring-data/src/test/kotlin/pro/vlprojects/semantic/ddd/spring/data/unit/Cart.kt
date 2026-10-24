package pro.vlprojects.semantic.ddd.spring.data.unit

import pro.vlprojects.semantic.ddd.spring.data.SpringDataAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.Revision
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class CartId(
    value: Long,
) : AbstractId<Long>(value)

class ItemAdded(
    meta: EventMeta,
    aggregateRevision: Revision<CartId>,
    val sku: String,
) : AbstractEvent<Cart, CartId>(meta, aggregateRevision) {

    companion object Factory {

        fun of(cart: Cart, sku: String) = ItemAdded(EventMeta.of<ItemAdded>(), cart.revision, sku)
    }
}

class Cart(
    identifier: CartId,
) : SpringDataAggregateRoot<Cart, CartId>() {

    init {
        this.identifier = identifier
    }

    override fun isNew() = true

    fun add(sku: String) = change {
        register(ItemAdded.of(this, sku))
    }
}
