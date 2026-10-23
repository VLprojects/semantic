package pro.vlprojects.semantic.ddd.unit.tactical.base

import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEntity
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import java.time.Instant

@JvmInline
value class CartId(
    val value: Long,
) : Identifier {

    override fun toString() = value.toString()
}

class ItemAdded : AbstractEvent<Cart, CartId> {

    val sku: String

    constructor(cart: Cart, sku: String) : super("cart.item-added", cart, Cart.NOW) {
        this.sku = sku
    }

    constructor(header: EventHeader, aggregateRevision: AggregateRevision<CartId>, sku: String) :
        super(header, aggregateRevision) {
        this.sku = sku
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
        register(ItemAdded(this, sku))
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
}

class WishList(
    identifier: CartId,
) : AbstractAggregateRoot<WishList, CartId>() {

    init {
        this.identifier = identifier
    }
}
