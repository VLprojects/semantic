package pro.vlprojects.semantic.ddd.unit.tactical.specification.composite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.specification.composite.AllOf
import pro.vlprojects.semantic.ddd.tactical.specification.composite.AnyOf

class AnyOfTests {

    private val positiveOrEven = AnyOf(listOf(Positive, Even))

    @Test
    fun `any of holds when one part holds`() {
        assertTrue(positiveOrEven.isSatisfiedBy(1))
        assertTrue(positiveOrEven.isSatisfiedBy(-2))
        assertFalse(positiveOrEven.isSatisfiedBy(-1))
    }

    @Test
    fun `any of is equal regardless of the order of its parts`() {
        assertEquals(positiveOrEven, AnyOf(listOf(Even, Positive)))
        assertEquals(positiveOrEven.hashCode(), AnyOf(listOf(Even, Positive)).hashCode())
    }

    @Test
    fun `any of is not all of`() {
        assertNotEquals(positiveOrEven, AllOf<Int>(listOf(Positive, Even)))
    }

    @Test
    fun `any of is written by its parts`() {
        assertEquals("(Positive or Even)", positiveOrEven.toString())
    }
}
