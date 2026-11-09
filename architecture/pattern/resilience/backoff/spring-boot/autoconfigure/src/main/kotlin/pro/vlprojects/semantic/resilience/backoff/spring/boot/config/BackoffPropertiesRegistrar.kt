package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties

internal class BackoffPropertiesRegistrar(private val properties: BackoffConfigurationProperties) : BackoffRegistrar {

    private val factory = BackoffFactory(properties.default)

    override fun register(registry: BackoffRegistry) = properties.instances
        .toList()
        .plus(BackoffRegistry.DEFAULT_NAME to BackoffProperties())
        .forEach { (name, instance) ->
            createBackoff(name, instance).let { backoff -> registry.register(name, backoff) }
        }

    private fun createBackoff(name: String, instance: BackoffProperties) = try {
        factory.create(instance)
    } catch (failure: IllegalArgumentException) {
        throw IllegalArgumentException("The backoff '$name' is misconfigured: ${failure.message}", failure)
    }
}
