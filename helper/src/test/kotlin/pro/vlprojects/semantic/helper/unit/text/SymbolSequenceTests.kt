package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.SymbolSequence

class SymbolSequenceTests {

    @Test
    fun `an emoji of several characters is one symbol`() {
        assertEquals(3, SymbolSequence("a👍🏽b").length)
    }

    @Test
    fun `an empty text has no symbols`() {
        assertEquals(IntRange.EMPTY, SymbolSequence("").indices)
    }

    @Test
    fun `a range of symbols is extracted whole`() {
        assertEquals("👍🏽b", SymbolSequence("a👍🏽b").extract(1..2))
    }

    @Test
    fun `a symbol is told by its first code point`() {
        val symbols = SymbolSequence("Ǆa1-")

        assertTrue(symbols.isCapitalAt(0))
        assertTrue(symbols.isLowercaseAt(1))
        assertTrue(symbols.isLetterOrDigitAt(2))
        assertFalse(symbols.isLetterOrDigitAt(3))
    }
}
