package pro.vlprojects.semantic.ddd.support

private const val EDGE_LENGTH = 4

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

internal fun Any.abbreviated() = toString().let { value ->
    when {
        value.length > 3 * EDGE_LENGTH -> "${value.take(EDGE_LENGTH)}…${value.takeLast(EDGE_LENGTH)}"
        else -> value
    }
}
