package pro.vlprojects.semantic.ddd.unit.tactical.specification.composite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.specification.composite.AllOf

class AllOfTests {

    private val positiveAndEven = AllOf(listOf(Positive, Even))

    @Test
    fun `all of holds when every part holds`() {
        assertTrue(positiveAndEven.isSatisfiedBy(2))
        assertFalse(positiveAndEven.isSatisfiedBy(1))
        assertFalse(positiveAndEven.isSatisfiedBy(-2))
    }

    @Test
    fun `all of is equal regardless of the order of its parts`() {
        assertEquals(positiveAndEven, AllOf(listOf(Even, Positive)))
        assertEquals(positiveAndEven.hashCode(), AllOf(listOf(Even, Positive)).hashCode())
    }

    @Test
    fun `all of counts a repeated part`() {
        assertNotEquals(AllOf(listOf(Positive, Positive, Even)), AllOf(listOf(Positive, Even, Even)))
    }

    @Test
    fun `all of is written by its parts`() {
        assertEquals("(Positive and Even)", positiveAndEven.toString())
    }

    @Test
    fun `all of lists nested parts flat`() {
        val nested = AllOf(listOf(AllOf(listOf(Positive, Even)), Positive))

        assertEquals(listOf(Positive, Even, Positive), nested.specifications)
    }

    @Test
    fun `all of is equal whatever the nesting of its parts`() {
        val left = AllOf(listOf(AllOf(listOf(Positive, Even)), Even))
        val right = AllOf(listOf(Positive, AllOf(listOf(Even, Even))))

        assertEquals(left, right)
        assertEquals(left.hashCode(), right.hashCode())
    }
}
