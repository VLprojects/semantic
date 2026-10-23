package pro.vlprojects.semantic.ddd.support

/**
 * The string in kebab case: `ShoppingCart` is `shopping-cart`. A word starts at a capital after a lower case letter or
 * a digit, and at the last capital of an acronym before a lower case letter: `HTTPRequest` is `http-request`.
 */
internal fun String.toKebabCase(): String =
    buildString {
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
