package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.core.Ordered
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType

internal class StandardBackoffRegistryCustomizer(
    private val properties: BackoffConfigurationProperties,
    private val backoffs: Map<String, Backoff>,
) : BackoffRegistryCustomizer, Ordered {

    private val factory = BackoffFactory(properties.default)

    override fun customize(registry: BackoffRegistry) {
        validateDefault()
        properties.instances
            .toList()
            .plus(BackoffRegistry.DEFAULT_NAME to BackoffProperties())
            .forEach { (name, instance) ->
                createBackoff(name, instance).let { backoff -> registry.register(name, backoff) }
            }
        backoffs.forEach { (name, backoff) -> registry.replace(name, backoff) }
    }

    override fun getOrder() = Ordered.HIGHEST_PRECEDENCE

    private fun validateDefault() = BackoffType.entries
        .map { type -> BackoffProperties(type = type) }
        .forEach { instance -> createBackoff(BackoffRegistry.DEFAULT_NAME, instance) }

    private fun createBackoff(name: String, instance: BackoffProperties) = try {
        factory.create(instance)
    } catch (failure: IllegalArgumentException) {
        throw IllegalArgumentException("The backoff '$name' is misconfigured: ${failure.message}", failure)
    }
}
