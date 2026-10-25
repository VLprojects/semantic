package pro.vlprojects.semantic.format

import pro.vlprojects.semantic.helper.text.SymbolSequence

public fun interface MaskMode {

    public fun mask(value: String, mask: Char): String

    public class Keep(private val first: Int = 0, private val last: Int = 0) : MaskMode {

        init {
            require(first >= 0) { "The count of first symbols must not be negative, got $first" }
            require(last >= 0) { "The count of last symbols must not be negative, got $last" }
        }

        override fun mask(value: String, mask: Char): String = value
            .let(::SymbolSequence)
            .mask(first, last, mask)
    }

    public data object HideAll : MaskMode {

        override fun mask(value: String, mask: Char): String = Keep().mask(value, mask)
    }

    public data object HideEmailName : MaskMode {

        override fun mask(value: String, mask: Char): String = when (val at = value.lastIndexOf('@')) {
            -1 -> HideAll.mask(value, mask)
            else -> value.maskName(at, mask)
        }

        private fun String.maskName(at: Int, mask: Char): String {
            val name = substring(0, at).let { Keep(first = 1).mask(it, mask) }
            val domain = substring(at)
            return "$name$domain"
        }
    }
}

private fun SymbolSequence.mask(first: Int, last: Int, mask: Char): String {
    val hidden = indices
        .filter(::isLetterOrDigitAt)
        .selectHidden(first, last)
    return indices.joinToString("") { index ->
        when (index) {
            in hidden -> mask.toString()
            else -> extract(index..index)
        }
    }
}

private fun List<Int>.selectHidden(first: Int, last: Int) = when {
    first + last >= size -> toSet()
    else -> subList(first, size - last).toSet()
}
