package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.toSymbolSequence

class SymbolSequenceTests {

    @Test
    fun `an emoji of several characters is one symbol`() {
        assertEquals(3, "a👍🏽b".toSymbolSequence().length)
    }

    @Test
    fun `an empty text has no symbols`() {
        assertEquals(IntRange.EMPTY, "".toSymbolSequence().indices)
    }

    @Test
    fun `a range of symbols is extracted whole`() {
        assertEquals("👍🏽b", "a👍🏽b".toSymbolSequence().extract(1..2))
    }

    @Test
    fun `a symbol is told by its first code point`() {
        val symbols = "Ǆa1-".toSymbolSequence()

        assertTrue(symbols.isCapitalAt(0))
        assertTrue(symbols.isLowercaseAt(1))
        assertTrue(symbols.isLetterOrDigitAt(2))
        assertFalse(symbols.isLetterOrDigitAt(3))
    }
}
