package pro.vlprojects.semantic.format.unit.mask

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.mask.MaskBeginning
import pro.vlprojects.semantic.format.mask.MaskEnding
import pro.vlprojects.semantic.format.mask.MaskMiddle
import pro.vlprojects.semantic.format.mask.masked

class StringMaskHelperTests {

    @Test
    fun `two first and two last symbols stay by default`() {
        assertEquals("42************42", "4242424242424242".masked())
    }

    @Test
    fun `the mode says where the kept symbols are`() {
        assertEquals("****ef", "abcdef".masked(keepLength = 2, mode = MaskBeginning))
        assertEquals("a****f", "abcdef".masked(keepLength = 2, mode = MaskMiddle))
        assertEquals("ab****", "abcdef".masked(keepLength = 2, mode = MaskEnding))
    }

    @Test
    fun `separators stay and only letters and digits are counted`() {
        assertEquals("+7 (9**) ***-**-*7", "+7 (916) 123-45-67".masked(keepLength = 3, mode = MaskMiddle))
        assertEquals("4242 42** **** ****", "4242 4242 4242 4242".masked(keepLength = 6, mode = MaskEnding))
    }

    @Test
    fun `a collapsed mask covers the masked part whole`() {
        assertEquals("42***42", "4242 4242 4242 4242".masked(collapseTo = 3))
        assertEquals("*** 4242", "4242 4242 4242 4242".masked(mode = MaskBeginning, collapseTo = 3))
    }

    @Test
    fun `a symbol of several characters is masked by one symbol`() {
        assertEquals("****", "Cafe\u0301".masked(keepLength = 0))
    }

    @Test
    fun `a value too short to mask anything is masked whole`() {
        assertEquals("****", "1234".masked())
        assertEquals("***", "1234".masked(collapseTo = 3))
    }

    @Test
    fun `a value with no letters or digits stays as it is`() {
        assertEquals("+-()", "+-()".masked())
    }

    @Test
    fun `the mask is any character`() {
        assertEquals("12••••78", "12345678".masked(symbol = '•'))
    }

    @Test
    fun `a mode picks the positions to mask`() {
        assertEquals(listOf(0, 1, 2), MaskBeginning.mask(listOf(0, 1, 2, 3), 1))
    }

    @Test
    fun `a mode of its own is a function`() {
        assertEquals("*ecret", "secret".masked(mode = { positions, _ -> positions.take(1) }))
    }

    @Test
    fun `a negative kept length or an empty collapse is rejected`() {
        assertThrows<IllegalArgumentException> { "secret".masked(keepLength = -1) }
        assertThrows<IllegalArgumentException> { "secret".masked(collapseTo = 0) }
    }
}
