package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

private const val PACKAGE_SEPARATOR = '.'

internal fun String.isPackageName() = this
    .split(PACKAGE_SEPARATOR)
    .all(String::isJavaIdentifier)

private fun String.isJavaIdentifier() = this.isNotEmpty() &&
    this.first().isJavaIdentifierStart() &&
    this.drop(1).all(Char::isJavaIdentifierPart)

internal fun String.isInPackage(packageName: String) = this.startsWith("$packageName$PACKAGE_SEPARATOR")

// Initialization is off so that no static initializer of an unchecked class runs.
internal fun String.loadClassOrNull(classLoader: ClassLoader) = try {
    Class.forName(this, false, classLoader)
} catch (_: ClassNotFoundException) {
    null
} catch (failure: LinkageError) {
    throw IllegalArgumentException("The class '$this' cannot be loaded", failure)
}

internal fun KClass<*>.getDefaultClassLoader() = Thread.currentThread()
    .contextClassLoader
    ?.takeIf { classLoader -> this.isVisible(classLoader) }
    ?: this.java.classLoader
    ?: ClassLoader.getSystemClassLoader()

private fun KClass<*>.isVisible(classLoader: ClassLoader) = this.java.name.loadClassOrNull(classLoader) == this.java
