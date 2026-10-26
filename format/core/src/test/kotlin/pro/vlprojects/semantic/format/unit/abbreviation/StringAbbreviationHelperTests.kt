package pro.vlprojects.semantic.format.unit.abbreviation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.abbreviation.PrefixAbbreviation
import pro.vlprojects.semantic.format.abbreviation.SuffixAbbreviation
import pro.vlprojects.semantic.format.abbreviation.abbreviated

class StringAbbreviationHelperTests {

    @Test
    fun `a long value keeps its start and end around the ellipsis`() {
        assertEquals("019…1d", ID.abbreviated())
    }

    @Test
    fun `the visible length can be set`() {
        assertEquals("0190…c41d", ID.abbreviated(visibleLength = 8))
    }

    @Test
    fun `the prefix can be abbreviated`() {
        assertEquals("…ec41d", ID.abbreviated(abbreviation = PrefixAbbreviation))
    }

    @Test
    fun `the suffix can be abbreviated`() {
        assertEquals("0190a…", ID.abbreviated(abbreviation = SuffixAbbreviation))
    }

    @Test
    fun `an abbreviation of its own shortens as it chooses`() {
        assertEquals("0190a3f2-7…", ID.abbreviated(abbreviation = { source, length -> "${source.take(length * 2)}…" }))
    }

    @Test
    fun `a value shorter than the minimum is kept whole`() {
        assertEquals("123456789", "123456789".abbreviated())
    }

    @Test
    fun `a value that would not get shorter is kept whole`() {
        assertEquals("1234567890", "1234567890".abbreviated(visibleLength = 9))
    }

    @Test
    fun `a character beyond the basic plane counts as one`() {
        assertEquals("ab😀…yz", "ab😀cdefghijklmnopqrstuvwxyz".abbreviated())
        assertEquals("😀😀😀😀😀😀😀😀😀", "😀😀😀😀😀😀😀😀😀".abbreviated())
        assertEquals("🇷🇺🇷🇺🇷🇺…🇷🇺🇷🇺", "🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺".abbreviated())
    }

    @Test
    fun `a visible length that is not positive is refused`() {
        assertThrows<IllegalArgumentException> { ID.abbreviated(visibleLength = 0) }
    }

    @Test
    fun `a minimum length not over the visible length is refused`() {
        assertThrows<IllegalArgumentException> { ID.abbreviated(minLength = 5, visibleLength = 5) }
    }

    private companion object {

        const val ID = "0190a3f2-7b1e-7c3a-9f4e-5d2b8a6ec41d"
    }
}
