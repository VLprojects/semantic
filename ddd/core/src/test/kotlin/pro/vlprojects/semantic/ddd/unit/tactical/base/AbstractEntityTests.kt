package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class AbstractEntityTests {

    @Test
    fun `entities with one identifier are equal whatever their state`() {
        val first = Cart(CartId(1)).add("apple")
        val second = Cart(CartId(1))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `entities with different identifiers differ`() {
        assertNotEquals(Cart(CartId(1)), Cart(CartId(2)))
    }

    @Test
    fun `an entity shows its class, kind and identifier`() {
        assertEquals("Line entity[id: 1]", Line(CartId(1)).toString())
    }

    @Test
    fun `an aggregate root shows its class, kind and identifier`() {
        assertEquals("Cart aggregate-root[id: 1]", Cart(CartId(1)).toString())
    }
}
