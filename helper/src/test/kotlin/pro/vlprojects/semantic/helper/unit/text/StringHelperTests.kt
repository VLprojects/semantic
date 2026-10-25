package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
}
