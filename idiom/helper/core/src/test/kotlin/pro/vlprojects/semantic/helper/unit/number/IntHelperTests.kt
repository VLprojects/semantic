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
import pro.vlprojects.semantic.helper.number.toSafeShort

class IntHelperTests {

    @Test
    fun `the sign of a number is told by its predicates`() {
        assertTrue(1.isPositive())
        assertTrue((-1).isNegative())
        assertTrue(0.isZero())
        assertTrue(1.isNotZero())
        assertTrue(0.isNonNegative())
    }

    @Test
    fun `zero is neither positive nor negative`() {
        assertFalse(0.isPositive())
        assertFalse(0.isNegative())
        assertFalse(0.isNotZero())
        assertFalse((-1).isNonNegative())
    }

    @Test
    fun `a number within the range narrows`() {
        assertEquals(Short.MAX_VALUE, 32767.toSafeShort())
        assertEquals(Short.MIN_VALUE, (-32768).toSafeShort())
        assertEquals(Byte.MAX_VALUE, 127.toSafeByte())
        assertEquals(Byte.MIN_VALUE, (-128).toSafeByte())
    }

    @Test
    fun `a number out of the range is refused`() {
        assertThrows<ArithmeticException> { 32768.toSafeShort() }
        assertThrows<ArithmeticException> { (-32769).toSafeShort() }
        assertThrows<ArithmeticException> { 128.toSafeByte() }
        assertThrows<ArithmeticException> { (-129).toSafeByte() }
    }
}
