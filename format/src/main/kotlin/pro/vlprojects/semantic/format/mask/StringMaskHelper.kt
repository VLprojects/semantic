package pro.vlprojects.semantic.format.mask

import pro.vlprojects.semantic.helper.text.SymbolSequence
import pro.vlprojects.semantic.helper.text.toSymbolSequence

public fun String.masked(
    keepLength: Int = 4,
    mode: MaskMode = MaskMiddle,
    symbol: Char = '*',
    collapseTo: Int? = null,
): String {

    require(keepLength >= 0) { "The kept length must not be negative, got $keepLength" }
    collapseTo?.let { require(it > 0) { "The collapsed length must be positive, got $it" } }

    val symbols = this.toSymbolSequence()

    val positions = symbols
        .indices
        .filter(symbols::isLetterOrDigitAt)

    val masked = when {
        keepLength >= positions.size -> positions
        else -> mode.mask(positions, keepLength)
    }

    return when {
        masked.isEmpty() -> this
        collapseTo == null -> symbols.coverEach(masked.toSet(), symbol)
        else -> symbols.coverSpan(masked, symbol, collapseTo)
    }
}

private fun SymbolSequence.coverEach(masked: Set<Int>, symbol: Char) = this
    .indices
    .joinToString("") { index ->
        when (index) {
            in masked -> symbol.toString()
            else -> extract(index..index)
        }
    }

private fun SymbolSequence.coverSpan(masked: List<Int>, symbol: Char, length: Int): String {

    val before = masked
        .first()
        .let { 0 until it }
        .let(::extract)

    val after = masked
        .last()
        .inc()
        .until(this.length)
        .let(::extract)

    val cover = symbol
        .toString()
        .repeat(length)

    return "$before$cover$after"
}
