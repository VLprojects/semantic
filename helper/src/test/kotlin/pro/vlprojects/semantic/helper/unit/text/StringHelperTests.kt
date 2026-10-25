package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.helper.text.endsWithAny
import pro.vlprojects.semantic.helper.text.isAscii
import pro.vlprojects.semantic.helper.text.isNotAscii
import pro.vlprojects.semantic.helper.text.notEndsWithAny
import pro.vlprojects.semantic.helper.text.notStartsWithAny
import pro.vlprojects.semantic.helper.text.startsWithAny
import pro.vlprojects.semantic.helper.text.symbolCount
import pro.vlprojects.semantic.helper.text.takeLastSymbols
import pro.vlprojects.semantic.helper.text.takeSymbols

class StringHelperTests {

    @Test
    fun `a character beyond the basic plane counts as one`() {
        assertEquals(3, "a😀b".symbolCount)
    }

    @Test
    fun `a letter with its combining mark counts as one`() {
        assertEquals(4, "Cafe\u0301".symbolCount)
    }

    @Test
    fun `an emoji made of several characters counts as one`() {
        assertEquals(1, "👨‍👩‍👧".symbolCount)
        assertEquals(1, "🇷🇺".symbolCount)
        assertEquals(1, "👍🏽".symbolCount)
        assertEquals(1, "❤️".symbolCount)
    }

    @Test
    fun `an emoji made of several characters is never cut`() {
        assertEquals("a👨‍👩‍👧", "a👨‍👩‍👧b".takeSymbols(2))
        assertEquals("🇷🇺b", "a🇷🇺b".takeLastSymbols(2))
    }

    @Test
    fun `an empty value has no symbols`() {
        assertEquals(0, "".symbolCount)
    }

    @Test
    fun `the first characters are taken whole`() {
        assertEquals("a😀", "a😀b".takeSymbols(2))
    }

    @Test
    fun `the last characters are taken whole`() {
        assertEquals("😀b", "a😀b".takeLastSymbols(2))
    }

    @Test
    fun `taking more than there is gives the whole value`() {
        assertEquals("a😀b", "a😀b".takeSymbols(5))
        assertEquals("a😀b", "a😀b".takeLastSymbols(5))
    }

    @Test
    fun `taking none gives an empty value`() {
        assertEquals("", "a😀b".takeSymbols(0))
        assertEquals("", "a😀b".takeLastSymbols(0))
    }

    @Test
    fun `a negative count is refused`() {
        assertThrows<IllegalArgumentException> { "a😀b".takeSymbols(-1) }
        assertThrows<IllegalArgumentException> { "a😀b".takeLastSymbols(-1) }
    }

    @Test
    fun `a value of latin letters, digits and punctuation is ascii`() {
        assertTrue("order-42_id.".isAscii())
        assertFalse("order-42_id.".isNotAscii())
    }

    @Test
    fun `a value with an accent or an emoji is not ascii`() {
        assertTrue("Café".isNotAscii())
        assertTrue("a😀".isNotAscii())
    }

    @Test
    fun `an empty value is ascii`() {
        assertTrue("".isAscii())
    }

    @Test
    fun `a value starts with any of the prefixes`() {
        assertTrue("order-42".startsWithAny("user-", "order-"))
        assertFalse("order-42".notStartsWithAny("user-", "order-"))
    }

    @Test
    fun `a value starts with none of the prefixes`() {
        assertFalse("order-42".startsWithAny("user-", "cart-"))
        assertTrue("order-42".notStartsWithAny("user-", "cart-"))
    }

    @Test
    fun `a value ends with any of the suffixes`() {
        assertTrue("report.pdf".endsWithAny(".doc", ".pdf"))
        assertFalse("report.pdf".notEndsWithAny(".doc", ".pdf"))
    }

    @Test
    fun `a value ends with none of the suffixes`() {
        assertFalse("report.pdf".endsWithAny(".doc", ".txt"))
        assertTrue("report.pdf".notEndsWithAny(".doc", ".txt"))
    }

    @Test
    fun `no prefixes and no suffixes match nothing`() {
        assertFalse("order-42".startsWithAny())
        assertFalse("order-42".endsWithAny())
    }
}
