package pro.vlprojects.semantic.format.mask

import pro.vlprojects.semantic.helper.text.SymbolSequence
import pro.vlprojects.semantic.helper.text.toSymbolSequence

public fun String.masked(
    visibleLength: Int = 4,
    masking: Masking = InfixMasking,
    maskChar: Char = '*',
    maskLength: Int? = null,
): String {

    require(visibleLength >= 0) { "The visible length must not be negative, got $visibleLength" }
    maskLength?.let { require(it > 0) { "The mask length must be positive, got $it" } }

    val symbols = this.toSymbolSequence()

    val maskableIndices = symbols
        .indices
        .filter(symbols::isLetterOrDigitAt)

    val maskedIndices = when {
        visibleLength >= maskableIndices.size -> maskableIndices
        else -> masking.mask(maskableIndices, visibleLength)
    }

    return when {
        maskedIndices.isEmpty() -> this
        maskLength == null -> symbols.maskEach(maskedIndices.toSet(), maskChar)
        else -> symbols.collapse(maskedIndices, maskChar, maskLength)
    }
}

private fun SymbolSequence.maskEach(maskedIndices: Set<Int>, maskChar: Char) = this
    .indices
    .joinToString("") { index ->
        when (index) {
            in maskedIndices -> maskChar.toString()
            else -> this.extract(index..index)
        }
    }

private fun SymbolSequence.collapse(maskedIndices: List<Int>, maskChar: Char, maskLength: Int): String {

    val before = maskedIndices
        .first()
        .let { 0 until it }
        .let(::extract)

    val after = maskedIndices
        .last()
        .inc()
        .until(this.length)
        .let(::extract)

    val mask = maskChar
        .toString()
        .repeat(maskLength)

    return "$before$mask$after"
}
