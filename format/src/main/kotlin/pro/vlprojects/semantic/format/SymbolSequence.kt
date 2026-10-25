package pro.vlprojects.semantic.format

import pro.vlprojects.semantic.helper.text.calculateSymbolBoundaryArray

internal class SymbolSequence(private val text: CharSequence) {

    private val boundaries = text.calculateSymbolBoundaryArray()

    val length = boundaries.lastIndex

    val indices = 0 until length

    fun extract(range: IntRange) = text.substring(boundaries[range.first], boundaries[range.last + 1])

    fun isLetterOrDigitAt(index: Int) = index
        .let(::readFirstCodePointAt)
        .let(Character::isLetterOrDigit)

    fun isLowercaseAt(index: Int) = index
        .let(::readFirstCodePointAt)
        .let(Character::isLowerCase)

    fun isCapitalAt(index: Int) = index
        .let(::readFirstCodePointAt)
        .let(::isCapital)

    private fun readFirstCodePointAt(index: Int) = boundaries[index]
        .let { Character.codePointAt(text, it) }

    private fun isCapital(codePoint: Int) = when {
        Character.isUpperCase(codePoint) -> true
        else -> Character.isTitleCase(codePoint)
    }
}
