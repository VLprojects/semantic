package pro.vlprojects.semantic.ddd.support

import kotlin.math.roundToInt

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

internal fun String.abbreviated(minLength: Int = 10, keep: Double = 0.5) = (minLength * keep).roundToInt().let { kept ->
    when {
        length < minLength -> this
        kept + 1 >= length -> this
        else -> "${take(kept - kept / 2)}…${takeLast(kept / 2)}"
    }
}
