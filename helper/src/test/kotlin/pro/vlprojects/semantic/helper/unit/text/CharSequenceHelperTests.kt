package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.calculateSymbolBoundaryArray
import pro.vlprojects.semantic.helper.text.hasFewerSymbolsThan

class CharSequenceHelperTests {

    @Test
    fun `the boundaries start each symbol and end the text`() {
        assertArrayEquals(intArrayOf(0, 1, 3, 4), "a😀b".calculateSymbolBoundaryArray())
    }

    @Test
    fun `an empty text has the one boundary at its end`() {
        assertArrayEquals(intArrayOf(0), "".calculateSymbolBoundaryArray())
    }

    @Test
    fun `fewer symbols than the count are counted as a reader sees them`() {
        assertTrue("🇷🇺👍🏽".hasFewerSymbolsThan(3))
        assertFalse("🇷🇺👍🏽".hasFewerSymbolsThan(2))
    }
}
