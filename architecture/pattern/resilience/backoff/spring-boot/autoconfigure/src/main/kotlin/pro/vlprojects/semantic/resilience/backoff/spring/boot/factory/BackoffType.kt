package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import org.springframework.aop.framework.AopProxyUtils
import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import org.springframework.core.GenericTypeResolver

private const val BEAN_NAME_SUFFIX = "BackoffFactory"
private const val NAME_SEPARATOR = '.'

internal class BackoffType<C : Any> private constructor(
    val name: ConfigurationPropertyName,
    val factory: BackoffFactory<C>,
    val configClass: Class<C>,
) {

    companion object Factory {

        @Suppress("UNCHECKED_CAST")
        fun of(beanName: String, factory: BackoffFactory<*>): BackoffType<*> {
            val name = readName(beanName)
            val configClass = resolveConfigClass(beanName, factory)

            return BackoffType(name, factory as BackoffFactory<Any>, configClass as Class<Any>)
        }

        // A bean name gives the type the way Spring Boot names health contributors: without the standard suffix.
        private fun readName(beanName: String) = beanName
            .let(::removeSuffix)
            .let { name -> ConfigurationPropertyName.adapt(name, NAME_SEPARATOR) }
            .takeIf(::isSingleElement)
            ?: throw IllegalArgumentException(
                "The backoff factory bean '$beanName' gives the type '${removeSuffix(beanName)}': " +
                    "it must be a single property name, so give the bean a short name",
            )

        private fun removeSuffix(beanName: String) = when {
            beanName.endsWith(BEAN_NAME_SUFFIX, ignoreCase = true) -> beanName.dropLast(BEAN_NAME_SUFFIX.length)
            else -> beanName
        }

        private fun isSingleElement(name: ConfigurationPropertyName) =
            name.numberOfElements == 1 && !name.isLastElementIndexed

        private fun resolveConfigClass(beanName: String, factory: BackoffFactory<*>) = factory
            .let(AopProxyUtils::ultimateTargetClass)
            .let { factoryClass -> GenericTypeResolver.resolveTypeArgument(factoryClass, BackoffFactory::class.java) }
            ?: throw IllegalArgumentException(
                "The backoff factory bean '$beanName' must declare its config class as the type argument of BackoffFactory",
            )
    }
}
