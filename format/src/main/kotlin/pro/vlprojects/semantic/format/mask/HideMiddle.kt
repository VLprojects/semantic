package pro.vlprojects.semantic.format.mask

import pro.vlprojects.semantic.helper.text.SymbolSequence

public class HideMiddle private constructor(private val keepFirst: Int, private val keepLast: Int) : MaskMode {

    override fun mask(source: String, mask: Char): String = source
        .let(::SymbolSequence)
        .mask(mask)

    private fun SymbolSequence.mask(mask: Char): String {
        val hidden = indices
            .filter(::isLetterOrDigitAt)
            .selectHidden()
        return indices.joinToString("") { index ->
            when (index) {
                in hidden -> mask.toString()
                else -> extract(index..index)
            }
        }
    }

    private fun List<Int>.selectHidden() = when {
        keepFirst + keepLast >= size -> toSet()
        else -> subList(keepFirst, size - keepLast).toSet()
    }

    public companion object Factory {

        public fun of(keepFirst: Int = 0, keepLast: Int = 0): HideMiddle {
            require(keepFirst >= 0) { "The count of first symbols to keep must not be negative, got $keepFirst" }
            require(keepLast >= 0) { "The count of last symbols to keep must not be negative, got $keepLast" }
            return HideMiddle(keepFirst, keepLast)
        }
    }
}
