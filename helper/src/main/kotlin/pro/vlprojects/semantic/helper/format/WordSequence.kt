package pro.vlprojects.semantic.helper.format

public interface WordSequence {

    public val length: Int

    public val indices: IntRange
        get() = 0 until length

    public operator fun get(index: Int): Word

    public fun subSequence(startIndex: Int, endIndex: Int): WordSequence

    public fun isEmpty(): Boolean = length == 0

    public operator fun iterator(): Iterator<Word> = this
        .indices
        .asSequence()
        .map(::get)
        .iterator()
}
