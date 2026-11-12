package pro.vlprojects.semantic.pattern.behavioral.resolver

import javax.lang.model.SourceVersion

private const val PACKAGE_SEPARATOR = '.'

internal fun String.isPackageName() = this.let(SourceVersion::isName)

internal fun String.isInPackage(packageName: String) = this.startsWith("$packageName$PACKAGE_SEPARATOR")
