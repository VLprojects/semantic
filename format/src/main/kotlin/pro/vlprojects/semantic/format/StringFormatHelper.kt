package pro.vlprojects.semantic.format

import pro.vlprojects.semantic.helper.text.hasFewerSymbolsThan
import pro.vlprojects.semantic.helper.text.toWordSequence

public fun String.toKebabCase(): String = this
    .toWordSequence()
    .toKebabCase()

public fun String.toSnakeCase(): String = this
    .toWordSequence()
    .toSnakeCase()

public fun String.toConstantCase(): String = this
    .toWordSequence()
    .toConstantCase()

public fun String.toCamelCase(): String = this
    .toWordSequence()
    .toCamelCase()

public fun String.toPascalCase(): String = this
    .toWordSequence()
    .toPascalCase()

public fun String.toTrainCase(): String = this
    .toWordSequence()
    .toTrainCase()

public fun String.toSentenceCase(): String = this
    .toWordSequence()
    .toSentenceCase()

public fun String.capitalizeEachPart(separator: String): String {
    require(separator.isNotEmpty()) { "The separator must not be empty" }
    return this
        .split(separator)
        .joinToString(separator, transform = String::capitalizeFirstLetter)
}

public fun String.capitalizeFirstLetter(): String = replaceFirstChar(Char::titlecase)

public fun String.abbreviated(
    minLength: Int = 10,
    keepLength: Int = 5,
    mode: EllipsisMode = EllipsisMode.HideMiddle,
): String {
    require(keepLength > 0) { "The kept length must be positive, got $keepLength" }
    require(minLength > keepLength) { "The minimum length $minLength must be over the kept length $keepLength" }
    return when {
        hasFewerSymbolsThan(minLength) -> this
        hasFewerSymbolsThan(keepLength + 2) -> this
        else -> mode.abbreviate(this, keepLength)
    }
}
