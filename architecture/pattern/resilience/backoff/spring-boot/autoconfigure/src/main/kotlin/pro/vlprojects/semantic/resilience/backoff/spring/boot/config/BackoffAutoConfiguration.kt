package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.DEFAULT_BACKOFF_NAME
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.ExponentialBackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory

@AutoConfiguration
@EnableConfigurationProperties(BackoffConfigurationProperties::class)
public class BackoffAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public fun fixedBackoffFactory(): FixedBackoffFactory = FixedBackoffFactory()

    @Bean
    @ConditionalOnMissingBean
    public fun exponentialBackoffFactory(): ExponentialBackoffFactory = ExponentialBackoffFactory()

    @Bean
    @ConditionalOnMissingBean
    public fun backoffRegistry(
        properties: BackoffConfigurationProperties,
        factories: List<BackoffFactory<*>>,
        backoffs: Map<String, Backoff>,
        customizers: ObjectProvider<BackoffRegistryCustomizer>,
    ): BackoffRegistry {
        val registry = BackoffRegistry()

        initBackoffRegistry(registry, properties, factories)
        backoffs.forEach { (name, backoff) -> registry.replace(name, backoff) }
        customizers
            .orderedStream()
            .forEach { customizer -> customizer.customize(registry) }

        return registry
    }

    private fun initBackoffRegistry(
        registry: BackoffRegistry,
        properties: BackoffConfigurationProperties,
        factories: List<BackoffFactory<*>>,
    ) {
        require(DEFAULT_BACKOFF_NAME !in properties.instances) {
            "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in the '$DEFAULT_BACKOFF_NAME' block"
        }

        val service = BackoffConfigurationService(properties.default, factories)

        properties.instances.forEach { (name, instance) -> registerBackoff(registry, service, name, instance) }
        registerBackoff(registry, service, DEFAULT_BACKOFF_NAME, BackoffProperties())
    }

    private fun registerBackoff(
        registry: BackoffRegistry,
        service: BackoffConfigurationService,
        name: String,
        properties: BackoffProperties,
    ) = service
        .create(name, properties)
        .let { backoff -> registry.register(name, backoff) }
}
