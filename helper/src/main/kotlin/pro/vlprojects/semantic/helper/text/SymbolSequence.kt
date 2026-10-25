package pro.vlprojects.semantic.helper.text

public class SymbolSequence(private val text: CharSequence) {

    private val boundaries = text.calculateSymbolBoundaryArray()

    public val length: Int = boundaries.lastIndex

    public val indices: IntRange = 0 until length

    public fun extract(range: IntRange): String = text.substring(boundaries[range.first], boundaries[range.last + 1])

    public fun isLetterOrDigitAt(index: Int): Boolean = index
        .let(::readFirstCodePointAt)
        .let(Character::isLetterOrDigit)

    public fun isLowercaseAt(index: Int): Boolean = index
        .let(::readFirstCodePointAt)
        .let(Character::isLowerCase)

    public fun isCapitalAt(index: Int): Boolean = index
        .let(::readFirstCodePointAt)
        .let(::isCapital)

    private fun readFirstCodePointAt(index: Int) = boundaries[index]
        .let { Character.codePointAt(text, it) }

    private fun isCapital(codePoint: Int) = when {
        Character.isUpperCase(codePoint) -> true
        else -> Character.isTitleCase(codePoint)
    }
}
