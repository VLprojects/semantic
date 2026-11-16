package pro.vlprojects.semantic.ddd.spring.data.mongodb.integration

import pro.vlprojects.semantic.ddd.spring.data.mongodb.AbstractMongoAggregateRoot
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
) : AbstractEvent<Cart, CartId>(meta, aggregateRevision)

class Cart(
    identifier: CartId,
) : AbstractMongoAggregateRoot<Cart, CartId>() {

    init {
        this.identifier = identifier
    }

    var items = 0
        private set

    fun add() = change {
        items += 1
        val meta = EventMeta.of<ItemAdded>()
        BaseRevision
            .of(this)
            .let { ItemAdded(meta, it) }
            .let { register(it) }
    }
}
