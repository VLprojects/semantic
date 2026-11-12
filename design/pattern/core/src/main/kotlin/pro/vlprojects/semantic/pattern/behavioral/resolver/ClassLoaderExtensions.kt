package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

// Initialization is off so that no static initializer of an unchecked class runs.
internal fun String.loadClassOrNull(classLoader: ClassLoader) = try {
    Class.forName(this, false, classLoader)
} catch (_: ClassNotFoundException) {
    null
} catch (failure: LinkageError) {
    throw IllegalArgumentException("The class '$this' cannot be loaded", failure)
}

internal fun KClass<*>.getDefaultClassLoader(resolverClassLoader: ClassLoader?) = resolverClassLoader
    ?.takeIf { classLoader -> this.isVisible(classLoader) }
    ?: this.java.classLoader
    ?: ClassLoader.getSystemClassLoader()

private fun KClass<*>.isVisible(classLoader: ClassLoader) = this.java.name.loadClassOrNull(classLoader) == this.java
