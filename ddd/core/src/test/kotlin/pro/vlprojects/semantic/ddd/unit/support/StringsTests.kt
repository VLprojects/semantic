package pro.vlprojects.semantic.ddd.unit.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.support.toKebabCase

class StringsTests {
    @Test
    fun `words of camel case are joined with dashes`() {
        assertEquals("shopping-cart", "ShoppingCart".toKebabCase())
    }

    @Test
    fun `an acronym is one word`() {
        assertEquals("http-request", "HTTPRequest".toKebabCase())
        assertEquals("order-id", "OrderID".toKebabCase())
    }

    @Test
    fun `a digit ends a word`() {
        assertEquals("cart2-item", "Cart2Item".toKebabCase())
    }

    @Test
    fun `a single word is lower cased`() {
        assertEquals("cart", "Cart".toKebabCase())
    }
}
