package pro.vlprojects.semantic.type.version.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import pro.vlprojects.semantic.type.version.SequentialVersion

class SequentialVersionTests {

    @Test
    fun `the initial version is zero`() {
        assertEquals(SequentialVersion.of(0), SequentialVersion.initial)
    }

    @Test
    fun `the next version is one more`() {
        assertEquals(SequentialVersion.of(4), SequentialVersion.of(3).next())
    }

    @Test
    fun `the last version has no next one`() {
        assertThrows<ArithmeticException> { SequentialVersion.of(Long.MAX_VALUE).next() }
    }

    @Test
    fun `a version is not negative`() {
        assertThrows<IllegalArgumentException> { SequentialVersion.of(-1) }
    }

    @Test
    fun `versions are ordered by their value`() {
        assertTrue(SequentialVersion.of(2) < SequentialVersion.of(10))
    }

    @Test
    fun `a version is parsed from its text`() {
        assertEquals(SequentialVersion.of(42), SequentialVersion.parse("42"))
    }

    @Test
    fun `a version shows its value`() {
        assertEquals("42", SequentialVersion.of(42).toString())
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "-1", "+1", "01", " 1", "1.0", "v1", "١", "9223372036854775808"])
    fun `only a canonical number is a version`(text: String) {
        assertNull(SequentialVersion.parseOrNull(text))
    }

    @Test
    fun `parsing a text that is not a version fails`() {
        assertThrows<IllegalArgumentException> { SequentialVersion.parse("v1") }
    }
}
