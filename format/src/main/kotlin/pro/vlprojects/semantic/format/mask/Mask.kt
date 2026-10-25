package pro.vlprojects.semantic.format.mask

import pro.vlprojects.semantic.helper.text.SymbolSequence

public class Mask private constructor(private val symbol: Char, private val length: Int?) {

    internal fun cover(symbols: SymbolSequence, hidden: List<Int>) = when (length) {
        null -> coverEach(symbols, hidden.toSet())
        else -> coverSpan(symbols, hidden, length)
    }

    private fun coverEach(symbols: SymbolSequence, hidden: Set<Int>) = symbols
        .indices
        .joinToString("") { index ->
            when (index) {
                in hidden -> symbol.toString()
                else -> symbols.extract(index..index)
            }
        }

    private fun coverSpan(symbols: SymbolSequence, hidden: List<Int>, length: Int): String {

        val before = hidden
            .first()
            .let { 0 until it }
            .let(symbols::extract)

        val after = hidden
            .last()
            .inc()
            .until(symbols.length)
            .let(symbols::extract)

        val cover = symbol
            .toString()
            .repeat(length)

        return "$before$cover$after"
    }

    public companion object Factory {

        public fun of(symbol: Char = '*', length: Int? = null): Mask {
            length?.let { require(it > 0) { "The length of a mask must be positive, got $it" } }

            return Mask(symbol, length)
        }
    }
}
