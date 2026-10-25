package pro.vlprojects.semantic.helper.text

public val String.symbolCount: Int
    get() = this
        .calculateAllSymbolsStartedAt()
        .count()
