package pro.vlprojects.semantic.pattern.enterprise.plugin

private const val KEY_SEPARATOR = '.'

private const val INDEX_START = '['

internal fun String.extractTypeName() = this
    .substringBefore(KEY_SEPARATOR)
    .substringBefore(INDEX_START)

internal fun Map<String, String>.extractFields() = this
    .filterKeys(String::hasFieldPath)
    .mapKeys { (key) -> key.extractFieldPath() }

private fun String.hasFieldPath() = when {
    this.extractTypeName().isEmpty() -> false
    else -> this.removeTypeName().isFieldPath()
}

private fun String.isFieldPath() = this.startsWith(KEY_SEPARATOR) && this.length > 1

private fun String.removeTypeName() = this
    .extractTypeName()
    .length
    .let { typeNameLength -> this.substring(typeNameLength) }

private fun String.extractFieldPath() = this.substringAfter(KEY_SEPARATOR)

internal fun Map.Entry<String, String>.isUnbound() = when {
    this.key.hasFieldPath() -> false
    this.key.isBlockKey() -> this.value.isNotEmpty()
    this.key.isTypeKey() -> this.value.isNotEmpty()
    else -> true
}

private fun String.isBlockKey() = this.isEmpty()

private fun String.isTypeKey() = when {
    this.isBlockKey() -> false
    else -> this.removeTypeName().isEmpty()
}

internal fun String.appendSegment(segment: String) = when {
    this.isEmpty() -> segment
    else -> "$this$KEY_SEPARATOR$segment"
}

internal fun String.appendKey(key: String) = when {
    key.isEmpty() -> this
    key.startsWith(INDEX_START) -> "$this$key"
    else -> this.appendSegment(key)
}
