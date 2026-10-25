package pro.vlprojects.semantic.ddd.spring.data.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.annotation.Transient
import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataEntity
import java.lang.reflect.Modifier

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
    fun `an entity is new until the store marks it persisted`() {
        assertTrue(Item(ItemId(1)).isNew)
        assertFalse(Item(ItemId(1)).loaded().isNew)
    }

    @Test
    fun `no store keeps the flag`() {
        val flag = AbstractSpringDataEntity::class.java.getDeclaredField("persisted")

        assertTrue(flag.modifiers.let(Modifier::isTransient))
        assertTrue(flag.isAnnotationPresent(Transient::class.java))
    }

    @Test
    fun `an entity equals its proxy`() {
        assertEquals(Item(ItemId(1)), ItemProxy(ItemId(1)))
        assertEquals(ItemProxy(ItemId(1)), Item(ItemId(1)))
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
