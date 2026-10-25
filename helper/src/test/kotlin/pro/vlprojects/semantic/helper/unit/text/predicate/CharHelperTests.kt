package pro.vlprojects.semantic.helper.unit.text.predicate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.predicate.isApostrophe
import pro.vlprojects.semantic.helper.text.predicate.isAscii
import pro.vlprojects.semantic.helper.text.predicate.isBracket
import pro.vlprojects.semantic.helper.text.predicate.isCarriageReturn
import pro.vlprojects.semantic.helper.text.predicate.isClosingBracket
import pro.vlprojects.semantic.helper.text.predicate.isClosingParenthesis
import pro.vlprojects.semantic.helper.text.predicate.isComma
import pro.vlprojects.semantic.helper.text.predicate.isDot
import pro.vlprojects.semantic.helper.text.predicate.isEquals
import pro.vlprojects.semantic.helper.text.predicate.isHorizontalTab
import pro.vlprojects.semantic.helper.text.predicate.isHyphen
import pro.vlprojects.semantic.helper.text.predicate.isNewline
import pro.vlprojects.semantic.helper.text.predicate.isOpeningBracket
import pro.vlprojects.semantic.helper.text.predicate.isOpeningParenthesis
import pro.vlprojects.semantic.helper.text.predicate.isParenthesis
import pro.vlprojects.semantic.helper.text.predicate.isQuote
import pro.vlprojects.semantic.helper.text.predicate.isSemicolon
import pro.vlprojects.semantic.helper.text.predicate.isSpace
import pro.vlprojects.semantic.helper.text.predicate.isSpaceSeparator
import pro.vlprojects.semantic.helper.text.predicate.isUnderscore
import pro.vlprojects.semantic.helper.text.predicate.isWildcard

class CharHelperTests {

    private val signs = " \t\n\r'\"=,;.-_*()[]"

    @Test
    fun `the last character of ascii is ascii`() {
        assertTrue('\u007F'.isAscii())
    }

    @Test
    fun `the first character beyond ascii is not ascii`() {
        assertFalse('\u0080'.isAscii())
        assertFalse('é'.isAscii())
    }

    @Test
    fun `each sign is recognized by its own predicate only`() {
        assertEquals(" ", signs.filter(Char::isSpace))
        assertEquals("\t", signs.filter(Char::isHorizontalTab))
        assertEquals("\n", signs.filter(Char::isNewline))
        assertEquals("\r", signs.filter(Char::isCarriageReturn))
        assertEquals("'", signs.filter(Char::isApostrophe))
        assertEquals("\"", signs.filter(Char::isQuote))
        assertEquals("=", signs.filter(Char::isEquals))
        assertEquals(",", signs.filter(Char::isComma))
        assertEquals(";", signs.filter(Char::isSemicolon))
        assertEquals(".", signs.filter(Char::isDot))
        assertEquals("-", signs.filter(Char::isHyphen))
        assertEquals("_", signs.filter(Char::isUnderscore))
        assertEquals("*", signs.filter(Char::isWildcard))
        assertEquals("(", signs.filter(Char::isOpeningParenthesis))
        assertEquals(")", signs.filter(Char::isClosingParenthesis))
        assertEquals("()", signs.filter(Char::isParenthesis))
        assertEquals("[", signs.filter(Char::isOpeningBracket))
        assertEquals("]", signs.filter(Char::isClosingBracket))
        assertEquals("[]", signs.filter(Char::isBracket))
    }

    @Test
    fun `a space of any width is a space separator`() {
        assertTrue(" \u00A0\u2009\u202F\u3000".all(Char::isSpaceSeparator))
    }

    @Test
    fun `a tab and a line break are not space separators`() {
        assertFalse("\t\n\r".any(Char::isSpaceSeparator))
    }

    @Test
    fun `a letter is none of the signs`() {
        assertFalse('a'.isHyphen())
        assertFalse('a'.isParenthesis())
        assertFalse('a'.isBracket())
    }
}
