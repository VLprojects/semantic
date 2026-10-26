package pro.vlprojects.semantic.helper.unit.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.helper.text.Word
import pro.vlprojects.semantic.helper.text.WordSequence
import pro.vlprojects.semantic.helper.text.asIterable
import pro.vlprojects.semantic.helper.text.toWordSequence

class WordSequenceTests {

    @Test
    fun `camel case is split where the case changes`() {
        assertEquals(listOf("Shopping", "Cart"), wordsOf("ShoppingCart"))
    }

    @Test
    fun `an acronym is one word`() {
        assertEquals(listOf("HTTP", "Request"), wordsOf("HTTPRequest"))
        assertEquals(listOf("Order", "ID"), wordsOf("OrderID"))
    }

    @Test
    fun `a digit ends a word`() {
        assertEquals(listOf("Cart2", "Item"), wordsOf("Cart2Item"))
    }

    @Test
    fun `any run of what is not a letter or a digit separates words`() {
        assertEquals(listOf("order", "line", "item", "id"), wordsOf(" order.line\titem -- id! "))
    }

    @Test
    fun `an emoji separates words`() {
        assertEquals(listOf("order", "id"), wordsOf("order😀id"))
    }

    @Test
    fun `a letter beyond the basic plane stays in its word`() {
        assertEquals(listOf("order𐐨𐐨"), wordsOf("order𐐨𐐨"))
    }

    @Test
    fun `a combining mark with nothing to mark is no word`() {
        assertEquals(listOf("cart"), wordsOf("\u0301cart"))
    }

    @Test
    fun `a value with no letters or digits has no words`() {
        assertEquals(emptyList<String>(), wordsOf("--"))
    }

    @Test
    fun `a word is given in lower case, in capitals or capitalized`() {
        val word = "HTTPRequest".toWordSequence()[0]
        assertEquals("http", word.lowercase())
        assertEquals("HTTP", word.uppercase())
        assertEquals("Http", word.capitalized())
    }

    @Test
    fun `a word is taken by its index`() {
        val words = "ShoppingCartItem".toWordSequence()
        assertEquals(3, words.length)
        assertEquals("Cart", words[1].toString())
    }

    @Test
    fun `a part of the sequence is a sequence`() {
        assertEquals("CartItem".toWordSequence(), "ShoppingCartItem".toWordSequence().subSequence(1, 3))
    }

    @Test
    fun `a sequence with no words is empty`() {
        assertEquals(true, "--".toWordSequence().isEmpty())
    }

    @Test
    fun `sequences of the same words are equal`() {
        assertEquals("order_id".toWordSequence(), "order-id".toWordSequence())
        assertEquals("order_id".toWordSequence().hashCode(), "order-id".toWordSequence().hashCode())
    }

    @Test
    fun `a sequence is made of words at hand`() {
        assertEquals("CartItem".toWordSequence(), listOf(Word.of("Cart"), Word.of("Item")).toWordSequence())
    }

    @Test
    fun `a sequence is written as its words with spaces`() {
        assertEquals("HTTP Request", "HTTPRequest".toWordSequence().toString())
    }

    @Test
    fun `a word is read as the chars it was written with`() {
        val word = Word.of("Cart")
        assertEquals(4, word.length)
        assertEquals('C', word[0])
        assertEquals("ar", word.subSequence(1, 3))
        assertEquals(true, word.startsWith("Ca"))
    }

    @Test
    fun `a word keeps the text it was made of`() {
        val text = StringBuilder("Cart")
        val word = Word.of(text)
        text.append("Item")
        assertEquals("Cart", word.toString())
    }

    @Test
    fun `a word may have its case change inside`() {
        assertEquals("iPhone", Word.of("iPhone").toString())
    }

    @Test
    fun `a word may have combining marks`() {
        assertEquals("Cafe\u0301", Word.of("Cafe\u0301").toString())
    }

    @Test
    fun `a word is refused when it is not only letters and digits`() {
        assertThrows<IllegalArgumentException> { Word.of("") }
        assertThrows<IllegalArgumentException> { Word.of(" ") }
        assertThrows<IllegalArgumentException> { Word.of("shopping cart") }
        assertThrows<IllegalArgumentException> { Word.of("--") }
        assertThrows<IllegalArgumentException> { Word.of("order😀") }
        assertThrows<IllegalArgumentException> { Word.of("\u0301cart") }
    }

    private fun wordsOf(value: String) = value
        .toWordSequence()
        .asIterable()
        .map(Word::toString)
}
