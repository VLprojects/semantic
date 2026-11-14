package pro.vlprojects.semantic.pattern.behavioral.resolver

// Initialization is off so that no static initializer runs before the base type is checked.
internal fun ClassLoader.loadClassOrNull(className: String) = try {
    Class.forName(className, false, this)
} catch (_: ClassNotFoundException) {
    null
} catch (failure: LinkageError) {
    throw IllegalArgumentException("The class '$className' cannot be loaded", failure)
}

internal fun Class<*>.getDefaultClassLoader(resolverClassLoader: ClassLoader?) = resolverClassLoader
    ?.takeIf { classLoader -> this.isVisible(classLoader) }
    ?: this.classLoader
    ?: ClassLoader.getSystemClassLoader()

private fun Class<*>.isVisible(classLoader: ClassLoader) = classLoader.loadClassOrNull(this.name) == this
