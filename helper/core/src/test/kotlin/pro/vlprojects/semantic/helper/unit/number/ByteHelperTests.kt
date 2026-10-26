package pro.vlprojects.semantic.helper.unit.number

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.number.isNegative
import pro.vlprojects.semantic.helper.number.isNonNegative
import pro.vlprojects.semantic.helper.number.isNotZero
import pro.vlprojects.semantic.helper.number.isPositive
import pro.vlprojects.semantic.helper.number.isZero

class ByteHelperTests {

    private val one: Byte = 1
    private val zero: Byte = 0
    private val minusOne: Byte = -1

    @Test
    fun `the sign of a number is told by its predicates`() {
        assertTrue(one.isPositive())
        assertTrue(minusOne.isNegative())
        assertTrue(zero.isZero())
        assertTrue(one.isNotZero())
        assertTrue(zero.isNonNegative())
        assertFalse(minusOne.isNonNegative())
    }
}
