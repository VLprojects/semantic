package pro.vlprojects.semantic.helper.text

import java.text.BreakIterator

internal fun CharSequence.calculateSymbolBoundaries() = this
    .toBreakIterator()
    .let { iterator ->
        generateSequence(iterator::first) {
            iterator.next()
        }
    }
    .takeWhile { it != BreakIterator.DONE }

internal fun CharSequence.calculateSymbolBoundaryArray() = this
    .toBreakIterator()
    .let { iterator ->
        IntArray(length + 1) { index ->
            when (index) {
                0 -> iterator.first()
                else -> iterator.next()
            }
        }
    }
    .let { boundaries ->
        boundaries
            .indexOf(length)
            .inc()
            .let(boundaries::copyOf)
    }

internal fun CharSequence.calculateReversedSymbolBoundaries() = this
    .toBreakIterator()
    .let { iterator ->
        generateSequence(iterator::last) {
            iterator.previous()
        }
    }
    .takeWhile { it != BreakIterator.DONE }

internal fun CharSequence.calculateAllSymbolsStartedAt() = this
    .calculateSymbolBoundaries()
    .takeWhile(indices::contains)

public fun CharSequence.hasFewerSymbolsThan(count: Int): Boolean = this
    .calculateAllSymbolsStartedAt()
    .take(count)
    .count() < count

private fun CharSequence.toBreakIterator() = BreakIterator
    .getCharacterInstance()
    .also { iterator ->
        toString().let(iterator::setText)
    }

public fun CharSequence.toWordSequence(): WordSequence = this
    .let(::SymbolSequence)
    .toWords()
    .let(::ListWordSequence)

internal fun CharSequence.isWord() = when {
    isEmpty() -> false
    else -> hasOnlyLetterOrDigitSymbols()
}

private fun CharSequence.hasOnlyLetterOrDigitSymbols() = this
    .let(::SymbolSequence)
    .hasOnlyLetterOrDigitSymbols()

private fun SymbolSequence.hasOnlyLetterOrDigitSymbols() = indices.all(::isLetterOrDigitAt)

private fun SymbolSequence.toWords() = this
    .indices
    .asSequence()
    .filter(::isWordBeginningAt)
    .map { it until findWordEndingPosition(it) }
    .map(::extract)
    .map(::Word)
    .toList()

private fun SymbolSequence.findWordEndingPosition(start: Int) = (start + 1 until length)
    .firstOrNull(::isWordFinishedAt)
    ?: length

private fun SymbolSequence.isWordBeginningAt(index: Int) = when {
    !isLetterOrDigitAt(index) -> false
    index == 0 -> true
    !isLetterOrDigitAt(index - 1) -> true
    else -> isCapitalWordBeginningAt(index)
}

private fun SymbolSequence.isWordFinishedAt(index: Int) = when {
    !isLetterOrDigitAt(index) -> true
    else -> isCapitalWordBeginningAt(index)
}

private fun SymbolSequence.isCapitalWordBeginningAt(index: Int) = when {
    !isCapitalAt(index) -> false
    !isCapitalAt(index - 1) -> true
    index + 1 == length -> false
    else -> isLowercaseAt(index + 1)
}
