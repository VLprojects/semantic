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

public fun CharSequence.calculateSymbolBoundaryArray(): IntArray = this
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
