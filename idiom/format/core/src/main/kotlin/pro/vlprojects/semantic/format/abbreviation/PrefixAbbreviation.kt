package pro.vlprojects.semantic.format.abbreviation

import pro.vlprojects.semantic.helper.text.transforming.takeLastSymbols

public data object PrefixAbbreviation : Abbreviation {

    override fun shorten(source: String, visibleLength: Int): String = source
        .takeLastSymbols(visibleLength)
        .let { "$ELLIPSIS$it" }
}
