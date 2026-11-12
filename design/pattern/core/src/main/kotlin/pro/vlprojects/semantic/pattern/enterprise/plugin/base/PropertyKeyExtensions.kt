package pro.vlprojects.semantic.pattern.enterprise.plugin.base

internal const val KEY_SEPARATOR = '.'

internal fun String.extractTypeName() = this.substringBefore(KEY_SEPARATOR)

internal fun String.extractFieldName() = this.substringAfter(KEY_SEPARATOR, missingDelimiterValue = "")
