package pro.vlprojects.semantic.format

import kotlin.math.absoluteValue

public fun Long.pluralize(one: String, few: String, many: String): String = when {
    lastTwoDigits in 11..14 -> many
    lastDigit == 1 -> one
    lastDigit in 2..4 -> few
    else -> many
}

private val Long.lastDigit
    get() = (this % 10)
        .absoluteValue
        .toInt()

private val Long.lastTwoDigits
    get() = (this % 100)
        .absoluteValue
        .toInt()
