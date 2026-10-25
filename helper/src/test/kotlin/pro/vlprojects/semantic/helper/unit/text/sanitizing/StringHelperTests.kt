package pro.vlprojects.semantic.helper.unit.text.sanitizing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.sanitizing.collapse
import pro.vlprojects.semantic.helper.text.sanitizing.collapseApostrophes
import pro.vlprojects.semantic.helper.text.sanitizing.collapseDots
import pro.vlprojects.semantic.helper.text.sanitizing.collapseHyphens
import pro.vlprojects.semantic.helper.text.sanitizing.collapseSpaces
import pro.vlprojects.semantic.helper.text.sanitizing.filterAscii
import pro.vlprojects.semantic.helper.text.sanitizing.normalizeSpaces
import pro.vlprojects.semantic.helper.text.sanitizing.removeDiacritics

class StringHelperTests {

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
    fun `the edges lose their whitespace and a run inside becomes one space`() {
        assertEquals("Anne Marie Curie", " \t Anne  Marie\n\nCurie \u00A0".normalizeSpaces())
    }

    @Test
    fun `a value of whitespace only becomes empty`() {
        assertEquals("", " \t\n ".normalizeSpaces())
        assertEquals("", "".normalizeSpaces())
    }
}
