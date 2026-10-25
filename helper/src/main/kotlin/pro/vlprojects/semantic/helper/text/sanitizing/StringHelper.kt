package pro.vlprojects.semantic.helper.text.sanitizing

import pro.vlprojects.semantic.helper.text.predicate.isApostrophe
import pro.vlprojects.semantic.helper.text.predicate.isAscii
import pro.vlprojects.semantic.helper.text.predicate.isDiacritic
import pro.vlprojects.semantic.helper.text.predicate.isDot
import pro.vlprojects.semantic.helper.text.predicate.isHyphen
import java.text.Normalizer

public fun String.filterAscii(): String = this.filter(Char::isAscii)

public fun String.collapseSpaces(): String = this.collapse(' ', Char::isWhitespace)

public fun String.normalizeSpaces(): String = this
    .trim()
    .collapseSpaces()

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
