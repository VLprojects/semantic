package pro.vlprojects.semantic.ddd.unit.tactical.specification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.specification.AllOf

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
}
