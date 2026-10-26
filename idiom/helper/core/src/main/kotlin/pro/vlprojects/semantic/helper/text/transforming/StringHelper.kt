package pro.vlprojects.semantic.helper.text.transforming

import pro.vlprojects.semantic.helper.text.calculateReversedSymbolBoundaries
import pro.vlprojects.semantic.helper.text.calculateSymbolBoundaries

public fun String.takeSymbols(count: Int): String {
    require(count >= 0) { "The count of symbols must not be negative, got $count" }
    return this
        .calculateSymbolBoundaries()
        .elementAtOrNull(count)
        ?.let { this.substring(0, it) }
        ?: this
}

public fun String.takeLastSymbols(count: Int): String {
    require(count >= 0) { "The count of symbols must not be negative, got $count" }
    return this
        .calculateReversedSymbolBoundaries()
        .elementAtOrNull(count)
        ?.let(::substring)
        ?: this
}
