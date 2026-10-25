package pro.vlprojects.semantic.helper.text

internal class ListWordSequence(private val words: List<Word>) : WordSequence {

    override val length = words.size

    override operator fun get(index: Int) = words[index]

    override fun subSequence(startIndex: Int, endIndex: Int) = words
        .subList(startIndex, endIndex)
        .let(::ListWordSequence)

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is ListWordSequence -> false
        else -> words == other.words
    }

    override fun hashCode() = words.hashCode()

    override fun toString() = words.joinToString(" ")
}
