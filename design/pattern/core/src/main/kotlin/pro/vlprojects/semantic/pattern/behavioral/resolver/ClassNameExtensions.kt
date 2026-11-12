package pro.vlprojects.semantic.pattern.behavioral.resolver

import javax.lang.model.SourceVersion

private const val PACKAGE_SEPARATOR = '.'

internal fun String.isPackageName() = this
    .split(PACKAGE_SEPARATOR)
    .all(SourceVersion::isIdentifier)

internal fun String.isInPackage(packageName: String) = this.startsWith("$packageName$PACKAGE_SEPARATOR")
