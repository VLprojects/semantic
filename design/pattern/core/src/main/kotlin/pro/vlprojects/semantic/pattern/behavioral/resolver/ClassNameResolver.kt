package pro.vlprojects.semantic.pattern.behavioral.resolver

import pro.vlprojects.semantic.pattern.behavioral.resolver.error.ClassNotAllowedException
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.NotSubtypeException
import kotlin.reflect.KClass

public class ClassNameResolver<T : Any> private constructor(
    private val baseType: KClass<T>,
    private val trustedPackages: Set<String>,
    private val classLoader: ClassLoader,
) : Resolver<String, KClass<out T>> {

    override fun resolveAll(context: String): Collection<KClass<out T>> = context
        .also(::checkIsTrusted)
        .loadClassOrNull(classLoader)
        ?.also(::checkIsSubtype)
        ?.asSubclass(baseType.java)
        ?.kotlin
        .let(::listOfNotNull)

    private fun checkIsTrusted(className: String) {
        if (trustedPackages.none(className::isInPackage)) {
            throw ClassNotAllowedException(className, trustedPackages)
        }
    }

    private fun checkIsSubtype(type: Class<*>) {
        if (!baseType.java.isAssignableFrom(type)) {
            throw NotSubtypeException(type.name, baseType)
        }
    }

    public companion object Factory {

        public fun <T : Any> of(
            baseType: KClass<T>,
            trustedPackages: Collection<String>,
            classLoader: ClassLoader = baseType.getDefaultClassLoader(),
        ): ClassNameResolver<T> = trustedPackages
            .toSet()
            .also(::checkHasTrustedPackage)
            .also(::checkHasNoInvalidPackageName)
            .let { packages -> ClassNameResolver(baseType, packages, classLoader) }
    }
}
