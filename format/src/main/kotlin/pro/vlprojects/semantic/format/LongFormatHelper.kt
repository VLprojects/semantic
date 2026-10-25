package pro.vlprojects.semantic.format

import kotlin.math.absoluteValue

public fun Long.pluralize(one: String, few: String, many: String): String = when {
    this.isEndingInElevenToFourteen() -> many
    this.isEndingInOne() -> one
    this.isEndingInTwoToFour() -> few
    else -> many
}

public fun Long.pluralizeWithCount(one: String, few: String, many: String): String =
    "$this ${this.pluralize(one, few, many)}"

private fun Long.isEndingInElevenToFourteen() = this.calculateRemainderOf(100) in 11..14

private fun Long.isEndingInOne() = this.calculateRemainderOf(10) == 1

private fun Long.isEndingInTwoToFour() = this.calculateRemainderOf(10) in 2..4

private fun Long.calculateRemainderOf(divisor: Int) = (this % divisor)
    .absoluteValue
    .toInt()
