package pro.vlprojects.semantic.format.mask

import pro.vlprojects.semantic.helper.text.SymbolSequence

public fun String.masked(keepLength: Int = 4, mode: MaskMode = MaskBeginning, mask: Mask = Mask.of()): String {
    require(keepLength >= 0) { "The kept length must not be negative, got $keepLength" }
    return mode.mask(this, keepLength, mask)
}

internal fun String.hide(keepFirst: Int, keepLast: Int, mask: Mask): String {
    val symbols = SymbolSequence(this)
    val hidden = symbols
        .indices
        .filter(symbols::isLetterOrDigitAt)
        .selectHidden(keepFirst, keepLast)
    return when {
        hidden.isEmpty() -> this
        else -> mask.cover(symbols, hidden)
    }
}

private fun List<Int>.selectHidden(keepFirst: Int, keepLast: Int) = when {
    keepFirst + keepLast >= size -> this
    else -> subList(keepFirst, size - keepLast)
}
