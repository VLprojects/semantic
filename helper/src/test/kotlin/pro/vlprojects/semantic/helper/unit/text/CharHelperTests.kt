package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.isAnySpace
import pro.vlprojects.semantic.helper.text.isApostrophe
import pro.vlprojects.semantic.helper.text.isAscii
import pro.vlprojects.semantic.helper.text.isBracket
import pro.vlprojects.semantic.helper.text.isCarriageReturn
import pro.vlprojects.semantic.helper.text.isClosingBracket
import pro.vlprojects.semantic.helper.text.isClosingParenthesis
import pro.vlprojects.semantic.helper.text.isComma
import pro.vlprojects.semantic.helper.text.isDot
import pro.vlprojects.semantic.helper.text.isEquals
import pro.vlprojects.semantic.helper.text.isHorizontalTab
import pro.vlprojects.semantic.helper.text.isHyphen
import pro.vlprojects.semantic.helper.text.isNewline
import pro.vlprojects.semantic.helper.text.isOpeningBracket
import pro.vlprojects.semantic.helper.text.isOpeningParenthesis
import pro.vlprojects.semantic.helper.text.isParenthesis
import pro.vlprojects.semantic.helper.text.isQuote
import pro.vlprojects.semantic.helper.text.isSemicolon
import pro.vlprojects.semantic.helper.text.isSpace
import pro.vlprojects.semantic.helper.text.isUnderscore
import pro.vlprojects.semantic.helper.text.isWildcard

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
    fun `a space of any width is any space`() {
        assertTrue(" \u00A0\u2009\u202F\u3000".all(Char::isAnySpace))
    }

    @Test
    fun `a tab and a line break are not spaces`() {
        assertFalse("\t\n\r".any(Char::isAnySpace))
    }

    @Test
    fun `a letter is none of the signs`() {
        assertFalse('a'.isHyphen())
        assertFalse('a'.isParenthesis())
        assertFalse('a'.isBracket())
    }
}
