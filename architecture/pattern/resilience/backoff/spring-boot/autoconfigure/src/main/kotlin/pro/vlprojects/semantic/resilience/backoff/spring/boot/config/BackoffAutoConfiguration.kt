package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffTypeRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.ExponentialBackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory

private const val FIXED_BACKOFF_FACTORY_BEAN_NAME = "fixedBackoffFactory"
private const val EXPONENTIAL_BACKOFF_FACTORY_BEAN_NAME = "exponentialBackoffFactory"

@AutoConfiguration
@EnableConfigurationProperties(BackoffProperties::class)
public class BackoffAutoConfiguration {

    // The bean name gives the type, so an application bean of that name replaces the algorithm.
    @Bean(FIXED_BACKOFF_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = [FIXED_BACKOFF_FACTORY_BEAN_NAME])
    public fun fixedBackoffFactory(): FixedBackoffFactory = FixedBackoffFactory()

    @Bean(EXPONENTIAL_BACKOFF_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = [EXPONENTIAL_BACKOFF_FACTORY_BEAN_NAME])
    public fun exponentialBackoffFactory(): ExponentialBackoffFactory = ExponentialBackoffFactory()

    @Bean
    @ConditionalOnMissingBean
    public fun backoffRegistry(
        properties: BackoffProperties,
        factories: Map<String, BackoffFactory<*>>,
        backoffs: Map<String, Backoff>,
        customizers: ObjectProvider<BackoffRegistryCustomizer>,
    ): BackoffRegistry {
        val types = BackoffTypeRegistry.of(factories)
        val registry = BackoffRegistry()

        BackoffPropertiesRegistrar(properties, types).registerBackoffs(registry)
        backoffs.forEach { (name, backoff) -> registry.replace(name, backoff) }
        customizers
            .orderedStream()
            .forEach { customizer -> customizer.customize(registry) }

        return registry
    }
}
