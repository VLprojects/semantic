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
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType

@AutoConfiguration
@EnableConfigurationProperties(BackoffConfigurationProperties::class)
public class BackoffAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public fun backoffRegistry(
        properties: BackoffConfigurationProperties,
        backoffs: Map<String, Backoff>,
        customizers: ObjectProvider<BackoffRegistryCustomizer>,
    ): BackoffRegistry {
        val registry = BackoffRegistry()

        initBackoffRegistry(registry, properties)
        backoffs.forEach { (name, backoff) -> registry.replace(name, backoff) }
        customizers
            .orderedStream()
            .forEach { customizer -> customizer.customize(registry) }

        return registry
    }

    private fun initBackoffRegistry(registry: BackoffRegistry, properties: BackoffConfigurationProperties) {
        val factory = BackoffFactory(properties.default)

        validateDefault(factory)
        properties.instances.forEach { (name, instance) -> registerBackoff(registry, factory, name, instance) }
        registerBackoff(registry, factory, DEFAULT_BACKOFF_NAME, BackoffProperties())
    }

    private fun validateDefault(factory: BackoffFactory) = BackoffType.entries
        .map { type -> BackoffProperties(type = type) }
        .forEach { instance -> createBackoff(factory, DEFAULT_BACKOFF_NAME, instance) }

    private fun registerBackoff(
        registry: BackoffRegistry,
        factory: BackoffFactory,
        name: String,
        properties: BackoffProperties,
    ) = createBackoff(factory, name, properties)
        .let { backoff -> registry.register(name, backoff) }

    private fun createBackoff(factory: BackoffFactory, name: String, properties: BackoffProperties) = try {
        factory.create(properties)
    } catch (failure: IllegalArgumentException) {
        throw IllegalArgumentException("The backoff '$name' is misconfigured: ${failure.message}", failure)
    }
}
