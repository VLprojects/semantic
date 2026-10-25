package pro.vlprojects.semantic.format.unit.mask

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.mask.InfixMasking
import pro.vlprojects.semantic.format.mask.PrefixMasking
import pro.vlprojects.semantic.format.mask.SuffixMasking
import pro.vlprojects.semantic.format.mask.masked

class StringMaskHelperTests {

    @Test
    fun `two first and two last symbols stay by default`() {
        assertEquals("42************42", "4242424242424242".masked())
    }

    @Test
    fun `the masking says where the visible symbols are`() {
        assertEquals("****ef", "abcdef".masked(visibleLength = 2, masking = PrefixMasking))
        assertEquals("a****f", "abcdef".masked(visibleLength = 2, masking = InfixMasking))
        assertEquals("ab****", "abcdef".masked(visibleLength = 2, masking = SuffixMasking))
    }

    @Test
    fun `separators stay and only letters and digits are counted`() {
        assertEquals("+7 (9**) ***-**-*7", "+7 (916) 123-45-67".masked(visibleLength = 3, masking = InfixMasking))
        assertEquals("4242 42** **** ****", "4242 4242 4242 4242".masked(visibleLength = 6, masking = SuffixMasking))
    }

    @Test
    fun `a mask of a given length covers the masked part whole`() {
        assertEquals("42***42", "4242 4242 4242 4242".masked(maskLength = 3))
        assertEquals("*** 4242", "4242 4242 4242 4242".masked(masking = PrefixMasking, maskLength = 3))
    }

    @Test
    fun `a symbol of several characters is masked by one symbol`() {
        assertEquals("****", "Cafe\u0301".masked(visibleLength = 0))
    }

    @Test
    fun `a value too short to mask anything is masked whole`() {
        assertEquals("****", "1234".masked())
        assertEquals("***", "1234".masked(maskLength = 3))
    }

    @Test
    fun `a value with no letters or digits stays as it is`() {
        assertEquals("+-()", "+-()".masked())
    }

    @Test
    fun `the mask is any character`() {
        assertEquals("12••••78", "12345678".masked(maskChar = '•'))
    }

    @Test
    fun `a masking picks the indices to cover`() {
        assertEquals(listOf(0, 1, 2), PrefixMasking.cover(listOf(0, 1, 2, 3), 1))
    }

    @Test
    fun `a masking of its own is a function`() {
        assertEquals("*ecret", "secret".masked(masking = { maskableIndices, _ -> maskableIndices.take(1) }))
    }

    @Test
    fun `a negative visible length or an empty mask is rejected`() {
        assertThrows<IllegalArgumentException> { "secret".masked(visibleLength = -1) }
        assertThrows<IllegalArgumentException> { "secret".masked(maskLength = 0) }
    }
}
