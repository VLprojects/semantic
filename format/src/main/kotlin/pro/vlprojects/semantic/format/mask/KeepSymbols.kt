package pro.vlprojects.semantic.format.mask

import pro.vlprojects.semantic.helper.text.SymbolSequence

public class KeepSymbols(private val first: Int = 0, private val last: Int = 0) : MaskMode {

    init {
        require(first >= 0) { "The count of first symbols must not be negative, got $first" }
        require(last >= 0) { "The count of last symbols must not be negative, got $last" }
    }

    override fun mask(value: String, mask: Char): String = value
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
        first + last >= size -> toSet()
        else -> subList(first, size - last).toSet()
    }
}
