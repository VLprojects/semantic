package pro.vlprojects.semantic.helper.format

public fun WordSequence.toKebabCase(): String = this
    .asSequence()
    .joinToString("-", transform = Word::lowercase)

public fun WordSequence.toSnakeCase(): String = this
    .asSequence()
    .joinToString("_", transform = Word::lowercase)

public fun WordSequence.toCamelCase(): String = this
    .asSequence()
    .mapIndexed(::formatCamelCaseWord)
    .joinToString("")

private fun formatCamelCaseWord(index: Int, word: Word) = when (index) {
    0 -> word.lowercase()
    else -> word.capitalized()
}
