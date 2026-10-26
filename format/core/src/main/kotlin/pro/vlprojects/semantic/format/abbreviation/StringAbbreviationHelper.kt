package pro.vlprojects.semantic.format.abbreviation

import pro.vlprojects.semantic.helper.text.predicate.hasFewerSymbolsThan

internal const val ELLIPSIS = "…"

public fun String.abbreviated(
    minLength: Int = 10,
    visibleLength: Int = 5,
    abbreviation: Abbreviation = InfixAbbreviation,
): String {

    require(visibleLength > 0) { "The visible length must be positive, got $visibleLength" }
    require(minLength > visibleLength) {
        "The minimum length $minLength must be over the visible length $visibleLength"
    }

    return when {
        this.hasFewerSymbolsThan(minLength) -> this
        this.hasFewerSymbolsThan(visibleLength + 2) -> this
        else -> abbreviation.shorten(this, visibleLength)
    }
}
