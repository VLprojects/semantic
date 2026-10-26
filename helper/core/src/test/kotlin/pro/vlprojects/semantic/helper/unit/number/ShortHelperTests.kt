package pro.vlprojects.semantic.helper.unit.number

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.helper.number.isNegative
import pro.vlprojects.semantic.helper.number.isNonNegative
import pro.vlprojects.semantic.helper.number.isNotZero
import pro.vlprojects.semantic.helper.number.isPositive
import pro.vlprojects.semantic.helper.number.isZero
import pro.vlprojects.semantic.helper.number.toSafeByte

class ShortHelperTests {

    private val one: Short = 1
    private val zero: Short = 0
    private val minusOne: Short = -1

    @Test
    fun `the sign of a number is told by its predicates`() {
        assertTrue(one.isPositive())
        assertTrue(minusOne.isNegative())
        assertTrue(zero.isZero())
        assertTrue(one.isNotZero())
        assertTrue(zero.isNonNegative())
        assertFalse(minusOne.isNonNegative())
    }

    @Test
    fun `a number within the range narrows`() {
        assertEquals(Byte.MIN_VALUE, Byte.MIN_VALUE.toShort().toSafeByte())
    }

    @Test
    fun `a number out of the range is refused`() {
        assertThrows<ArithmeticException> { Short.MAX_VALUE.toSafeByte() }
    }
}
