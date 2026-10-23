package pro.vlprojects.semantic.ddd.support

internal fun String.toKebabCase() = buildString {
    this@toKebabCase.forEachIndexed { index, char ->
        if (index > 0 && char.isUpperCase() && startsWord(index)) append('-')
        append(char.lowercaseChar())
    }
}

private fun String.startsWord(index: Int): Boolean {
    val previous = this[index - 1]
    return when {
        previous.isLowerCase() -> true
        previous.isDigit() -> true
        else -> previous.isUpperCase() && getOrNull(index + 1)?.isLowerCase() == true
    }
}

internal fun String.abbreviated(head: Int = 4, tail: Int = 4) = when {
    length > head + tail + 1 -> "${take(head)}…${takeLast(tail)}"
    else -> this
}
