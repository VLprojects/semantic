package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.symbolCount

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
    fun `an empty value has no symbols`() {
        assertEquals(0, "".symbolCount)
    }
}
