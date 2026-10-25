package pro.vlprojects.semantic.format.unit.mask

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.mask.Mask
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
    fun `a collapsed mask covers the hidden part whole`() {
        assertEquals("*** 4242", "4242 4242 4242 4242".masked(mode = MaskBeginning, mask = Mask.of(length = 3)))
        assertEquals("se***ue", "secret-token-value".masked(mode = MaskMiddle, mask = Mask.of(length = 3)))
    }

    @Test
    fun `a symbol of several characters is hidden by one mask`() {
        assertEquals("****", "Cafe\u0301".masked(keepLength = 0))
    }

    @Test
    fun `a value too short to hide anything is hidden whole`() {
        assertEquals("****", "1234".masked())
        assertEquals("***", "1234".masked(mask = Mask.of(length = 3)))
    }

    @Test
    fun `a value with no letters or digits stays as it is`() {
        assertEquals("+-()", "+-()".masked())
    }

    @Test
    fun `the mask is any character`() {
        assertEquals("12••••78", "12345678".masked(mask = Mask.of(symbol = '•')))
    }

    @Test
    fun `a mode masks with a star by default`() {
        assertEquals("****ef", MaskBeginning.mask("abcdef", 2))
    }

    @Test
    fun `a mode of its own is a function`() {
        assertEquals("[hidden]", "secret".masked(mode = { _, _, _ -> "[hidden]" }))
    }

    @Test
    fun `a negative kept length or an empty mask is rejected`() {
        assertThrows<IllegalArgumentException> { "secret".masked(keepLength = -1) }
        assertThrows<IllegalArgumentException> { Mask.of(length = 0) }
    }
}
