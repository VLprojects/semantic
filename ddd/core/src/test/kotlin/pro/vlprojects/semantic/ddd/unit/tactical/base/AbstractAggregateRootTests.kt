package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision

class AbstractAggregateRootTests {

    @Test
    fun `a change registers its events in order`() {
        val cart = Cart(CartId(1)).add("apple").add("pear")

        assertEquals(listOf("apple", "pear"), cart.released().map { (it as ItemAdded).sku })
    }

    @Test
    fun `an event names the revision it happened at`() {
        val event = Cart(CartId(1), version = 3).add("apple").released().single()

        assertEquals(BaseRevision("cart", CartId(1), 3), event.aggregateRevision)
        assertEquals("cart.item-added", event.header.name)
        assertEquals(Cart.NOW, event.header.occurredAt)
    }

    @Test
    fun `a change that breaks an invariant throws`() {
        val cart = Cart(CartId(1)).add("apple").add("pear")

        val failure = assertThrows<BusinessRuleException> { cart.add("plum") }

        assertEquals("Cart 1 holds more than 2 items", failure.message)
    }

    @Test
    fun `saved events are forgotten`() {
        val cart = Cart(CartId(1)).add("apple")

        cart.saved()

        assertEquals(emptyList<Any>(), cart.released())
    }
}
