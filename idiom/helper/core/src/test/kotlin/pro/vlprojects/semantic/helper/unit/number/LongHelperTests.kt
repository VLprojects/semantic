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
import pro.vlprojects.semantic.helper.number.toSafeInt
import pro.vlprojects.semantic.helper.number.toSafeShort

class LongHelperTests {

    @Test
    fun `the sign of a number is told by its predicates`() {
        assertTrue(1L.isPositive())
        assertTrue((-1L).isNegative())
        assertTrue(0L.isZero())
        assertTrue(1L.isNotZero())
        assertTrue(0L.isNonNegative())
        assertFalse((-1L).isNonNegative())
    }

    @Test
    fun `a number within the range narrows`() {
        assertEquals(Int.MAX_VALUE, Int.MAX_VALUE.toLong().toSafeInt())
        assertEquals(Short.MIN_VALUE, (-32768L).toSafeShort())
        assertEquals(Byte.MAX_VALUE, 127L.toSafeByte())
    }

    @Test
    fun `a number out of the range is refused`() {
        assertThrows<ArithmeticException> { Long.MAX_VALUE.toSafeInt() }
        assertThrows<ArithmeticException> { 32768L.toSafeShort() }
        assertThrows<ArithmeticException> { Long.MIN_VALUE.toSafeByte() }
    }
}
