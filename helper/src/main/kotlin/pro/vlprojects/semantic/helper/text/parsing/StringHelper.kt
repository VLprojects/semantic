package pro.vlprojects.semantic.helper.text.parsing

import pro.vlprojects.semantic.helper.text.calculateAllSymbolsStartedAt

public val String.symbolCount: Int
    get() = this
        .calculateAllSymbolsStartedAt()
        .count()
