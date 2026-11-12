package pro.vlprojects.semantic.pattern.behavioral.resolver

internal fun Class<*>.extractTypeName(excludeBaseType: Class<*>) = when (this.simpleName) {
    excludeBaseType.simpleName -> this.simpleName
    else -> this.simpleName.removeSuffix(excludeBaseType.simpleName)
}

internal fun String.toUniformTypeName() = this
    .lowercase()
    .replace("-", "")
    .replace("_", "")
