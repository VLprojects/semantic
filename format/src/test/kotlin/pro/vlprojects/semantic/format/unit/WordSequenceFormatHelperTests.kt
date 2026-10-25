package pro.vlprojects.semantic.format.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.format.toCamelCase
import pro.vlprojects.semantic.format.toKebabCase
import pro.vlprojects.semantic.format.toSnakeCase
import pro.vlprojects.semantic.helper.text.Word
import pro.vlprojects.semantic.helper.text.WordSequence

class WordSequenceFormatHelperTests {

    @Test
    fun `words of a sequence of its own are written in every case`() {
        val words = KeptWords(Word.of("iPhone"), Word.of("Case"))
        assertEquals("iphone-case", words.toKebabCase())
        assertEquals("iphone_case", words.toSnakeCase())
        assertEquals("iphoneCase", words.toCamelCase())
    }

    private class KeptWords(vararg val words: Word) : WordSequence {

        override val length = words.size

        override fun get(index: Int) = words[index]

        override fun subSequence(startIndex: Int, endIndex: Int) = words
            .copyOfRange(startIndex, endIndex)
            .let { KeptWords(*it) }
    }
}
