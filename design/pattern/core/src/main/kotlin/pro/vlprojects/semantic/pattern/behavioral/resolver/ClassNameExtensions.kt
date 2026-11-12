package pro.vlprojects.semantic.pattern.behavioral.resolver

private const val PACKAGE_SEPARATOR = '.'

internal fun String.isPackageName() = this
    .split(PACKAGE_SEPARATOR)
    .all(String::isJavaIdentifier)

private fun String.isJavaIdentifier() = this.isNotEmpty() &&
    this.codePointAt(0).let(Character::isJavaIdentifierStart) &&
    this
        .codePoints()
        .skip(1)
        .allMatch(Character::isJavaIdentifierPart)

internal fun String.isInPackage(packageName: String) = this.startsWith("$packageName$PACKAGE_SEPARATOR")
