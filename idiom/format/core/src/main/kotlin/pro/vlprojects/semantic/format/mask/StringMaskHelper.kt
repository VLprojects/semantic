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

    val coveredIndices = symbols
        .findLetterOrDigitIndices()
        .coverWith(masking, visibleLength)

    return when {
        coveredIndices.isEmpty() -> this
        maskLength == null -> symbols.replaceEachCoveredSymbol(coveredIndices, maskChar)
        else -> symbols.replaceCoveredSpan(coveredIndices, maskChar, maskLength)
    }
}

private fun SymbolSequence.findLetterOrDigitIndices() = this
    .indices
    .filter(::isLetterOrDigitAt)

private fun List<Int>.coverWith(masking: Masking, visibleLength: Int) = when {
    this.size <= visibleLength -> this
    else -> masking.cover(this, visibleLength)
}

private fun SymbolSequence.replaceEachCoveredSymbol(coveredIndices: List<Int>, maskChar: Char): String {

    val covered = coveredIndices.toSet()

    return this
        .indices
        .joinToString("") { index ->
            when (index) {
                in covered -> maskChar.toString()
                else -> this.extract(index..index)
            }
        }
}

private fun SymbolSequence.replaceCoveredSpan(coveredIndices: List<Int>, maskChar: Char, maskLength: Int): String {

    val before = coveredIndices
        .first()
        .let { 0 until it }
        .let(::extract)

    val mask = maskChar
        .toString()
        .repeat(maskLength)

    val after = coveredIndices
        .last()
        .inc()
        .until(this.length)
        .let(::extract)

    return "$before$mask$after"
}
