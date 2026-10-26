package pro.vlprojects.semantic.format.abbreviation

import pro.vlprojects.semantic.helper.text.transforming.takeLastSymbols
import pro.vlprojects.semantic.helper.text.transforming.takeSymbols

public data object InfixAbbreviation : Abbreviation {

    override fun shorten(source: String, visibleLength: Int): String {

        val prefix = source.takeSymbols(visibleLength - visibleLength / 2)

        val suffix = source.takeLastSymbols(visibleLength / 2)

        return "$prefix$ELLIPSIS$suffix"
    }
}
