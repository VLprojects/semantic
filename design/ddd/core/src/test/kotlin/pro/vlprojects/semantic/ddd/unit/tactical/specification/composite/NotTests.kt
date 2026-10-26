package pro.vlprojects.semantic.ddd.unit.tactical.specification.composite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.specification.composite.Not

class NotTests {

    @Test
    fun `not holds when its part does not`() {
        assertTrue(Not(Positive).isSatisfiedBy(-1))
        assertFalse(Not(Positive).isSatisfiedBy(1))
    }

    @Test
    fun `not is equal by its part`() {
        assertEquals(Not(Positive), Not(Positive))
        assertEquals(Not(Positive).hashCode(), Not(Positive).hashCode())
        assertNotEquals(Not(Positive), Not(Even))
    }

    @Test
    fun `not is written by its part`() {
        assertEquals("not Positive", Not(Positive).toString())
    }
}
