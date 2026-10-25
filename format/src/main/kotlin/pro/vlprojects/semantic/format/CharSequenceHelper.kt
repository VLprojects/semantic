package pro.vlprojects.semantic.format

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
