package pro.vlprojects.semantic.helper.text

import java.text.Normalizer

public val String.symbolCount: Int
    get() = this
        .calculateAllSymbolsStartedAt()
        .count()

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

public fun String.isAscii(): Boolean = this.all(Char::isAscii)

public fun String.isNotAscii(): Boolean = !this.isAscii()

public fun String.filterAscii(): String = this.filter(Char::isAscii)

public fun String.isLowercase(): Boolean = this.none(Char::isCapital)

public fun String.startsWithAny(vararg prefixes: String): Boolean = prefixes.any(::startsWith)

public fun String.startsWithAny(prefixes: Iterable<String>): Boolean = prefixes.any(::startsWith)

public fun String.endsWithAny(vararg suffixes: String): Boolean = suffixes.any(::endsWith)

public fun String.endsWithAny(suffixes: Iterable<String>): Boolean = suffixes.any(::endsWith)

public fun String.notStartsWithAny(vararg prefixes: String): Boolean = !this.startsWithAny(*prefixes)

public fun String.notStartsWithAny(prefixes: Iterable<String>): Boolean = !this.startsWithAny(prefixes)

public fun String.notEndsWithAny(vararg suffixes: String): Boolean = !this.endsWithAny(*suffixes)

public fun String.notEndsWithAny(suffixes: Iterable<String>): Boolean = !this.endsWithAny(suffixes)

public fun String.collapseSpaces(): String = this.collapse(' ', Char::isWhitespace)

public fun String.collapseHyphens(): String = this.collapse('-', Char::isHyphen)

public fun String.collapseApostrophes(): String = this.collapse('\'', Char::isApostrophe)

public fun String.collapseDots(): String = this.collapse('.', Char::isDot)

public fun String.collapse(replacement: Char, isCollapsible: (Char) -> Boolean): String = this
    .let { source ->
        buildString(source.length) {
            source.forEachIndexed { index, char ->
                when {
                    !isCollapsible(char) -> append(char)
                    !source.hasCollapsibleBefore(index, isCollapsible) -> append(replacement)
                }
            }
        }
    }

public fun String.collapse(replacement: Char, collapsible: Iterable<Char>): String = collapsible
    .toSet()
    .let { chars ->
        this.collapse(replacement, chars::contains)
    }

public fun String.removeDiacritics(): String = this
    .takeUnless(String::isAscii)
    ?.normalizeWith(Normalizer.Form.NFD)
    ?.filterNot(Char::isDiacritic)
    ?.normalizeWith(Normalizer.Form.NFC)
    ?: this

private fun String.hasCollapsibleBefore(index: Int, isCollapsible: (Char) -> Boolean) = when (index) {
    0 -> false
    else -> this[index - 1].let(isCollapsible)
}

private fun String.normalizeWith(form: Normalizer.Form) = Normalizer.normalize(this, form)

private fun Char.isDiacritic() = this.category == CharCategory.NON_SPACING_MARK

private fun Char.isCapital() = when {
    this.isUpperCase() -> true
    this.isTitleCase() -> true
    else -> false
}
