package pro.vlprojects.semantic.helper.text

public class Word internal constructor(private val text: String) : CharSequence {

    override val length: Int = text.length

    override operator fun get(index: Int): Char = text[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = text.subSequence(startIndex, endIndex)

    public fun lowercase(): String = text.lowercase()

    public fun capitalized(): String = lowercase().replaceFirstChar(Char::titlecase)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Word -> false
        else -> text == other.text
    }

    override fun hashCode(): Int = text.hashCode()

    override fun toString(): String = text

    public companion object Factory {

        public fun of(value: CharSequence): Word = value
            .toString()
            .also(::checkIsWord)
            .let(::Word)

        private fun checkIsWord(value: String) = require(value.isWord()) {
            "A word must contain only letters and digits, got \"$value\""
        }
    }
}
