package pro.vlprojects.semantic.helper.text

public val String.symbolCount: Int
    get() = this
        .calculateAllSymbolsStartedAt()
        .count()

public fun String.takeSymbols(count: Int): String {
    require(count >= 0) { "The count of symbols must not be negative, got $count" }
    return this
        .calculateSymbolBoundaries()
        .elementAtOrNull(count)
        ?.let { substring(0, it) }
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
