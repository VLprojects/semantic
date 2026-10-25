package pro.vlprojects.semantic.helper.text.predicate

import pro.vlprojects.semantic.helper.text.calculateAllSymbolsStartedAt

public fun CharSequence.hasFewerSymbolsThan(count: Int): Boolean = this
    .calculateAllSymbolsStartedAt()
    .take(count)
    .count() < count
