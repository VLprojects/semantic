package pro.vlprojects.semantic.format

import pro.vlprojects.semantic.helper.text.Word
import pro.vlprojects.semantic.helper.text.WordSequence
import pro.vlprojects.semantic.helper.text.asSequence

public fun WordSequence.toKebabCase(): String = this
    .asSequence()
    .joinToString("-", transform = Word::lowercase)

public fun WordSequence.toSnakeCase(): String = this
    .asSequence()
    .joinToString("_", transform = Word::lowercase)

public fun WordSequence.toConstantCase(): String = this
    .asSequence()
    .joinToString("_", transform = Word::uppercase)

public fun WordSequence.toCamelCase(): String = this
    .asSequence()
    .mapIndexed(::formatCamelCaseWord)
    .joinToString("")

public fun WordSequence.toPascalCase(): String = this
    .asSequence()
    .joinToString("", transform = Word::capitalized)

public fun WordSequence.toTrainCase(): String = this
    .asSequence()
    .joinToString("-", transform = Word::capitalized)

public fun WordSequence.toSentenceCase(): String = this
    .asSequence()
    .mapIndexed(::formatSentenceCaseWord)
    .joinToString(" ")

private fun formatCamelCaseWord(index: Int, word: Word) = when (index) {
    0 -> word.lowercase()
    else -> word.capitalized()
}

private fun formatSentenceCaseWord(index: Int, word: Word) = when (index) {
    0 -> word.capitalized()
    else -> word.lowercase()
}
