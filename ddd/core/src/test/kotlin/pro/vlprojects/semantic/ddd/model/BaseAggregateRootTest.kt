package pro.vlprojects.semantic.ddd.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.error.BusinessRuleException
import pro.vlprojects.semantic.ddd.event.BaseDomainEvent
import pro.vlprojects.semantic.ddd.identity.Id
import pro.vlprojects.semantic.ddd.rule.BusinessRule
import pro.vlprojects.semantic.ddd.rule.rule

class BaseAggregateRootTest {
    @Test
    fun `a change records its events`() {
        val cart = Cart(CartId(1)).add("apple").add("pear")

        assertEquals(listOf("apple", "pear"), cart.peekEvents().map { it.item })
    }

    @Test
    fun `peeking leaves the events in place`() {
        val cart = Cart(CartId(1)).add("apple")

        cart.peekEvents()

        assertEquals(1, cart.peekEvents().size)
    }

    @Test
    fun `pulling takes the events and clears them`() {
        val cart = Cart(CartId(1)).add("apple")

        assertEquals(1, cart.pullEvents().size)
        assertTrue(cart.peekEvents().isEmpty())
    }

    @Test
    fun `a broken invariant throws and drops the events of the change`() {
        val cart = Cart(CartId(1)).add("apple").add("pear")

        val failure = assertThrows<BusinessRuleException> { cart.add("plum") }

        assertEquals("Cart 1 holds more than 2 items", failure.message)
        assertEquals(listOf("apple", "pear"), cart.peekEvents().map { it.item })
    }

    @Test
    fun `a failed change drops its events`() {
        val cart = Cart(CartId(1))

        assertThrows<IllegalStateException> { cart.addThenFail("apple") }

        assertTrue(cart.peekEvents().isEmpty())
    }

    @JvmInline
    private value class CartId(override val value: Int) : Id<Int>

    private class ItemAdded(override val aggregate: CartId, val item: String) : BaseDomainEvent<CartId>() {
        override val eventType: String get() = "cart.item-added"
    }

    private class Cart(override val identifier: CartId) : BaseAggregateRoot<CartId, ItemAdded>() {
        private val items = mutableListOf<String>()

        override val invariants: List<BusinessRule<Nothing>> =
            listOf(rule<Cart>({ items.size <= 2 }) { "Cart ${identifier.value} holds more than 2 items" })

        fun add(item: String) = change {
            items += item
            record(ItemAdded(identifier, item))
        }

        fun addThenFail(item: String) = change {
            record(ItemAdded(identifier, item))
            error("Failed after recording")
        }
    }
}
