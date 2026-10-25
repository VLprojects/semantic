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
        IntArray(this.length + 1) { index ->
            when (index) {
                0 -> iterator.first()
                else -> iterator.next()
            }
        }
    }
    .let { boundaries ->
        boundaries
            .indexOf(this.length)
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
    .takeWhile(this.indices::contains)

public fun CharSequence.hasFewerSymbolsThan(count: Int): Boolean = this
    .calculateAllSymbolsStartedAt()
    .take(count)
    .count() < count

private fun CharSequence.toBreakIterator() = BreakIterator
    .getCharacterInstance()
    .also { iterator ->
        this.toString().let(iterator::setText)
    }

public fun CharSequence.toSymbolSequence(): SymbolSequence = SymbolSequence.of(this)

public fun CharSequence.toWordSequence(): WordSequence = this
    .toSymbolSequence()
    .toWords()
    .let(::ListWordSequence)

internal fun CharSequence.isWord() = when {
    this.isEmpty() -> false
    else -> this.hasOnlyLetterOrDigitSymbols()
}

private fun CharSequence.hasOnlyLetterOrDigitSymbols() = this
    .toSymbolSequence()
    .hasOnlyLetterOrDigitSymbols()

private fun SymbolSequence.hasOnlyLetterOrDigitSymbols() = this.indices.all(::isLetterOrDigitAt)

private fun SymbolSequence.toWords() = this
    .indices
    .asSequence()
    .filter(::isWordBeginningAt)
    .map { it until this.findWordEndingPosition(it) }
    .map(::extract)
    .map(::Word)
    .toList()

private fun SymbolSequence.findWordEndingPosition(start: Int) = (start + 1 until this.length)
    .firstOrNull(::isWordFinishedAt)
    ?: this.length

private fun SymbolSequence.isWordBeginningAt(index: Int) = when {
    !this.isLetterOrDigitAt(index) -> false
    index == 0 -> true
    !this.isLetterOrDigitAt(index - 1) -> true
    else -> this.isCapitalWordBeginningAt(index)
}

private fun SymbolSequence.isWordFinishedAt(index: Int) = when {
    !this.isLetterOrDigitAt(index) -> true
    else -> this.isCapitalWordBeginningAt(index)
}

private fun SymbolSequence.isCapitalWordBeginningAt(index: Int) = when {
    !this.isCapitalAt(index) -> false
    !this.isCapitalAt(index - 1) -> true
    index + 1 == this.length -> false
    else -> this.isLowercaseAt(index + 1)
}
