package pro.vlprojects.semantic.ddd.unit.tactical.base

import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.EventHeader
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEntity
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision
import pro.vlprojects.semantic.ddd.tactical.base.SimpleEventHeader
import pro.vlprojects.semantic.ddd.tactical.base.UuidEventId
import java.time.Instant

@JvmInline
value class CartId(
    val value: Long,
) : Identifier {

    override fun toString() = value.toString()
}

class ItemAdded(
    header: EventHeader,
    aggregate: AggregateRevision<CartId>,
    val sku: String,
) : AbstractEvent<Cart, CartId>(header, aggregate)

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
        SimpleEventHeader(UuidEventId.random(), "cart.item-added", NOW)
            .let { ItemAdded(it, BaseRevision.of(this), sku) }
            .let { register(it) }
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
