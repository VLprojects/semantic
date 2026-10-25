package pro.vlprojects.semantic.format.mask

import pro.vlprojects.semantic.helper.text.toSymbolSequence

public fun String.masked(keepLength: Int = 4, mode: MaskMode = MaskMiddle, mask: Mask = Mask.of()): String {
    require(keepLength >= 0) { "The kept length must not be negative, got $keepLength" }

    return mode.mask(this, keepLength, mask)
}

internal fun String.mask(keepFirst: Int, keepLast: Int, mask: Mask): String {

    val symbols = this.toSymbolSequence()

    val hidden = symbols
        .indices
        .filter(symbols::isLetterOrDigitAt)
        .dropKept(keepFirst, keepLast)

    return when {
        hidden.isEmpty() -> this
        else -> mask.cover(symbols, hidden)
    }
}

private fun List<Int>.dropKept(keepFirst: Int, keepLast: Int) = when {
    keepFirst + keepLast >= size -> this
    else -> subList(keepFirst, size - keepLast)
}
