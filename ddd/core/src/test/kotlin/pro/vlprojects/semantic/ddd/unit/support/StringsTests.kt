package pro.vlprojects.semantic.ddd.unit.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.support.abbreviated
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

    @Test
    fun `a long value keeps its start and end`() {
        assertEquals("0190…c41d", "0190a3f2-7b1e-7c3a-9f4e-5d2b8a6ec41d".abbreviated())
    }

    @Test
    fun `a short value is kept whole`() {
        assertEquals("42", 42.abbreviated())
    }
}
