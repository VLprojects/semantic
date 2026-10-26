package pro.vlprojects.semantic.helper.unit.text.transforming

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.helper.text.transforming.takeLastSymbols
import pro.vlprojects.semantic.helper.text.transforming.takeSymbols

class StringHelperTests {

    @Test
    fun `an emoji made of several characters is never cut`() {
        assertEquals("a👨‍👩‍👧", "a👨‍👩‍👧b".takeSymbols(2))
        assertEquals("🇷🇺b", "a🇷🇺b".takeLastSymbols(2))
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
