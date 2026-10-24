package pro.vlprojects.semantic.ddd.spring.boot.integration

import org.springframework.context.event.EventListener
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class CartId(
    value: Long,
) : AbstractId<Long>(value)

class CartOpened(
    cart: Cart,
) : AbstractEvent<Cart, CartId>(EventMeta.of<CartOpened>(), cart.revision)

class Cart(
    identifier: CartId,
) : AbstractAggregateRoot<Cart, CartId>() {

    init {
        this.identifier = identifier
        register(CartOpened(this))
    }
}

class Listener {

    val received = mutableListOf<Event<*, *>>()

    @EventListener
    fun on(event: Event<*, *>) {
        received += event
    }
}
