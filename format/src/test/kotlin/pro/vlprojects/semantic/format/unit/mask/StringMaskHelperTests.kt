package pro.vlprojects.semantic.format.unit.mask

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.mask.HideAll
import pro.vlprojects.semantic.format.mask.KeepSymbols
import pro.vlprojects.semantic.format.mask.masked

class StringMaskHelperTests {

    @Test
    fun `the last four symbols stay by default`() {
        assertEquals("************4242", "4242424242424242".masked())
    }

    @Test
    fun `separators stay and only letters and digits are counted`() {
        assertEquals("+7 (***) ***-**-67", "+7 (916) 123-45-67".masked(KeepSymbols(first = 1, last = 2)))
        assertEquals("4242 42** **** 4242", "4242 4242 4242 4242".masked(KeepSymbols(first = 6, last = 4)))
    }

    @Test
    fun `a symbol of several characters is hidden by one mask`() {
        assertEquals("****", "Café".masked(HideAll))
    }

    @Test
    fun `a value too short to hide anything is hidden whole`() {
        assertEquals("****", "1234".masked())
        assertEquals("**-**", "12-34".masked(KeepSymbols(first = 2, last = 2)))
    }

    @Test
    fun `the mask is any character`() {
        assertEquals("••••5678", "12345678".masked(mask = '•'))
    }

    @Test
    fun `a mode of its own is a function`() {
        assertEquals("[hidden]", "secret".masked(mode = { _, _ -> "[hidden]" }))
    }

    @Test
    fun `a negative count is rejected`() {
        assertThrows<IllegalArgumentException> { KeepSymbols(first = -1) }
        assertThrows<IllegalArgumentException> { KeepSymbols(last = -1) }
    }
}
