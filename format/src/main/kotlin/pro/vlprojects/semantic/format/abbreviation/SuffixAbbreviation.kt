package pro.vlprojects.semantic.format.abbreviation

import pro.vlprojects.semantic.helper.text.transforming.takeSymbols

public data object SuffixAbbreviation : Abbreviation {

    override fun shorten(source: String, visibleLength: Int): String = source
        .takeSymbols(visibleLength)
        .let { "$it$ELLIPSIS" }
}
