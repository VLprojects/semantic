package pro.vlprojects.semantic.helper.format

import pro.vlprojects.semantic.helper.text.hasFewerSymbolsThan

public fun String.toKebabCase(): String = this
    .toWordSequence()
    .toKebabCase()

public fun String.toSnakeCase(): String = this
    .toWordSequence()
    .toSnakeCase()

public fun String.toCamelCase(): String = this
    .toWordSequence()
    .toCamelCase()

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
