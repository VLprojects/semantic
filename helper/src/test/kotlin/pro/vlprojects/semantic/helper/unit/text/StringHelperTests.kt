package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.helper.text.collapse
import pro.vlprojects.semantic.helper.text.collapseApostrophes
import pro.vlprojects.semantic.helper.text.collapseDots
import pro.vlprojects.semantic.helper.text.collapseHyphens
import pro.vlprojects.semantic.helper.text.collapseSpaces
import pro.vlprojects.semantic.helper.text.endsWithAny
import pro.vlprojects.semantic.helper.text.filterAscii
import pro.vlprojects.semantic.helper.text.isAscii
import pro.vlprojects.semantic.helper.text.isLowercase
import pro.vlprojects.semantic.helper.text.isNotAscii
import pro.vlprojects.semantic.helper.text.notEndsWithAny
import pro.vlprojects.semantic.helper.text.notStartsWithAny
import pro.vlprojects.semantic.helper.text.removeDiacritics
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

    @Test
    fun `the prefixes may come as a collection`() {
        assertTrue("order-42".startsWithAny(listOf("user-", "order-")))
        assertTrue("order-42".notStartsWithAny(setOf("user-", "cart-")))
    }

    @Test
    fun `the suffixes may come as a collection`() {
        assertTrue("report.pdf".endsWithAny(listOf(".doc", ".pdf")))
        assertTrue("report.pdf".notEndsWithAny(setOf(".doc", ".txt")))
    }

    @Test
    fun `an empty collection matches nothing`() {
        assertFalse("order-42".startsWithAny(emptyList()))
        assertFalse("order-42".endsWithAny(emptySet()))
    }

    @Test
    fun `a run of whitespace becomes one space`() {
        assertEquals("Anne Marie Curie", "Anne  \t Marie\n\nCurie".collapseSpaces())
        assertEquals("Anne Marie", "Anne\u00A0\u2009Marie".collapseSpaces())
    }

    @Test
    fun `a run of hyphens, apostrophes or dots becomes one`() {
        assertEquals("Jean-Luc", "Jean---Luc".collapseHyphens())
        assertEquals("O'Neil", "O''Neil".collapseApostrophes())
        assertEquals("a.b.", "a..b...".collapseDots())
    }

    @Test
    fun `a collapse keeps the other characters`() {
        assertEquals("a - b", "a - b".collapseHyphens())
        assertEquals("", "".collapseSpaces())
    }

    @Test
    fun `a run of the chosen characters becomes the replacement`() {
        assertEquals("order_42_id", "order  42\t\tid".collapse('_', Char::isWhitespace))
        assertEquals("a-b-c", "a-_b_-_c".collapse('-', listOf('-', '_')))
    }

    @Test
    fun `a replacement already in the value is not taken for a run`() {
        assertEquals("a__b", "a_ b".collapse('_', Char::isWhitespace))
    }

    @Test
    fun `no characters to collapse keep the value`() {
        assertEquals("a  b", "a  b".collapse('_', emptyList()))
    }

    @Test
    fun `a letter loses its accent`() {
        assertEquals("Cafe creme", "Café crème".removeDiacritics())
        assertEquals("Ecole", "E\u0301cole".removeDiacritics())
        assertEquals("Angstrom", "Ångstrom".removeDiacritics())
    }

    @Test
    fun `a text without accents stays as it is`() {
        assertEquals("Hello", "Hello".removeDiacritics())
        assertEquals("Привет 👋", "Привет 👋".removeDiacritics())
        assertEquals("한국어", "한국어".removeDiacritics())
    }

    @Test
    fun `a cyrillic short letter loses its breve`() {
        assertEquals("Иеи", "Йёй".removeDiacritics())
    }

    @Test
    fun `only the ascii characters are kept`() {
        assertEquals("Caf crme ", "Café crème 👋".filterAscii())
        assertEquals("order-42", "order-42".filterAscii())
    }

    @Test
    fun `a value without capitals is lowercase`() {
        assertTrue("order-id 42".isLowercase())
        assertTrue("заказ".isLowercase())
        assertTrue("".isLowercase())
    }

    @Test
    fun `a value with a capital is not lowercase`() {
        assertFalse("orderId".isLowercase())
        assertFalse("Заказ".isLowercase())
        assertFalse("\u01C5".isLowercase())
    }
}
