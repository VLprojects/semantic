package pro.vlprojects.semantic.ddd.spring.data.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class AbstractSpringDataEntityTests {

    @Test
    fun `the id is the identifier`() {
        assertEquals(ItemId(1), Item(ItemId(1)).id)
    }

    @Test
    fun `entities with one identifier are equal whatever their state`() {
        val first = Cart(CartId(1)).add("apple")
        val second = Cart(CartId(1))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `entities with different identifiers differ`() {
        assertNotEquals(Item(ItemId(1)), Item(ItemId(2)))
    }

    @Test
    fun `an entity shows its class, kind and identifier`() {
        assertEquals("Item [entity][id: 1]", Item(ItemId(1)).toString())
    }
}
