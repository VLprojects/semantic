package pro.vlprojects.semantic.ddd.support

internal fun String.toKebabCase() = buildString {
    this@toKebabCase.forEachIndexed { index, char ->
        if (index > 0 && char.isUpperCase() && startsWord(index)) append('-')
        append(char.lowercaseChar())
    }
}

private fun String.startsWord(index: Int): Boolean {
    val previous = this[index - 1]
    if (previous.isLowerCase() || previous.isDigit()) return true
    return previous.isUpperCase() && index + 1 < length && this[index + 1].isLowerCase()
}
