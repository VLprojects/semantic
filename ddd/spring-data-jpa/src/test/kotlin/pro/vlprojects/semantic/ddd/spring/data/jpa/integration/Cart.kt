package pro.vlprojects.semantic.ddd.spring.data.jpa.integration

import pro.vlprojects.semantic.ddd.spring.data.jpa.AbstractJpaAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.Revision
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity

@Embeddable
class CartId(
    value: Long,
) : LongId(value)

class ItemAdded(
    meta: EventMeta,
    aggregateRevision: Revision<CartId>,
) : AbstractEvent<Cart, CartId>(meta, aggregateRevision)

@Entity
class Cart(
    identifier: CartId,
) : AbstractJpaAggregateRoot<Cart, CartId>() {

    init {
        this.identifier = identifier
    }

    var items = 0
        protected set

    fun add() = change {
        items += 1
        register(ItemAdded(EventMeta.of<ItemAdded>(), revision))
    }
}
