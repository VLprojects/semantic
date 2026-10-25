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
import pro.vlprojects.semantic.helper.number.toSafeDouble
import pro.vlprojects.semantic.helper.number.toSafeFloat
import pro.vlprojects.semantic.helper.number.toSafeInt
import pro.vlprojects.semantic.helper.number.toSafeLong
import pro.vlprojects.semantic.helper.number.toSafeShort
import java.math.BigDecimal

class BigDecimalHelperTests {

    @Test
    fun `the sign of a number is told by its predicates`() {
        assertTrue(BigDecimal("0.01").isPositive())
        assertTrue(BigDecimal("-0.01").isNegative())
        assertTrue(BigDecimal("0.00").isZero())
        assertTrue(BigDecimal("0.01").isNotZero())
        assertTrue(BigDecimal.ZERO.isNonNegative())
        assertFalse(BigDecimal("-0.01").isNonNegative())
    }

    @Test
    fun `a whole number within the range narrows`() {
        assertEquals(Long.MAX_VALUE, BigDecimal.valueOf(Long.MAX_VALUE).toSafeLong())
        assertEquals(42, BigDecimal("42.00").toSafeInt())
        assertEquals(Short.MIN_VALUE, BigDecimal("-32768").toSafeShort())
        assertEquals(Byte.MAX_VALUE, BigDecimal("127").toSafeByte())
    }

    @Test
    fun `a whole number out of the range is refused`() {
        assertThrows<ArithmeticException> { BigDecimal("9223372036854775808").toSafeLong() }
        assertThrows<ArithmeticException> { BigDecimal("2147483648").toSafeInt() }
        assertThrows<ArithmeticException> { BigDecimal("32768").toSafeShort() }
        assertThrows<ArithmeticException> { BigDecimal("128").toSafeByte() }
    }

    @Test
    fun `a fraction is refused by a whole type`() {
        assertThrows<ArithmeticException> { BigDecimal("1.5").toSafeInt() }
    }

    @Test
    fun `a number within the range of a floating type converts`() {
        assertEquals(0.1, BigDecimal("0.1").toSafeDouble())
        assertEquals(0.1f, BigDecimal("0.1").toSafeFloat())
    }

    @Test
    fun `a number out of the range of a floating type is refused`() {
        assertThrows<ArithmeticException> { BigDecimal("1E+400").toSafeDouble() }
        assertThrows<ArithmeticException> { BigDecimal("-1E+40").toSafeFloat() }
    }
}
