package pro.vlprojects.semantic.pattern.behavioral.resolver

import pro.vlprojects.semantic.pattern.behavioral.resolver.error.ClassNotAllowedException
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.NotSubtypeException
import kotlin.reflect.KClass

public class ClassNameResolver<T : Any> private constructor(
    private val baseType: KClass<T>,
    private val allowedPackages: Set<String>,
    private val classLoader: ClassLoader,
) : Resolver<String, KClass<out T>> {

    override fun resolveAll(context: String): Collection<KClass<out T>> = context
        .also(::checkIsAllowed)
        .let(classLoader::loadClassOrNull)
        ?.also(::checkIsSubtype)
        ?.asSubclass(baseType.javaObjectType)
        ?.kotlin
        .let(::listOfNotNull)

    private fun checkIsAllowed(className: String) {
        if (allowedPackages.none(className::isInPackage)) {
            throw ClassNotAllowedException(className, allowedPackages)
        }
    }

    private fun checkIsSubtype(type: Class<*>) {
        if (!baseType.javaObjectType.isAssignableFrom(type)) {
            throw NotSubtypeException(type.name, baseType.javaObjectType.name)
        }
    }

    public companion object Factory {

        public fun <T : Any> of(
            baseType: KClass<T>,
            allowedPackages: Collection<String>,
            classLoader: ClassLoader = baseType.getDefaultClassLoader(ClassNameResolver::class.java.classLoader),
        ): ClassNameResolver<T> = allowedPackages
            .toSet()
            .also(::checkHasAllowedPackage)
            .also(::checkHasOnlyPackageNames)
            .let { packages -> ClassNameResolver(baseType, packages, classLoader) }
    }
}
