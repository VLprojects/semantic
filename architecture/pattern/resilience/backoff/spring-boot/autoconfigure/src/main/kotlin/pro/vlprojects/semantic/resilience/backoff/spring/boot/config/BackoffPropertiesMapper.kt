package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties

internal class BackoffPropertiesMapper(private val properties: BackoffConfigurationProperties) {

    private val factory = BackoffFactory(properties.default)

    fun asBackoffs() = properties.instances
        .also(::rejectReservedName)
        .plus(BackoffRegistry.DEFAULT_NAME to BackoffProperties())
        .mapValues { (name, instance) -> createBackoff(name, instance) }

    private fun rejectReservedName(instances: Map<String, BackoffProperties>) =
        check(BackoffRegistry.DEFAULT_NAME !in instances) {
            "The backoff '${BackoffRegistry.DEFAULT_NAME}' is set by the default block and cannot be an instance"
        }

    private fun createBackoff(name: String, instance: BackoffProperties) = try {
        factory.create(instance)
    } catch (failure: IllegalArgumentException) {
        throw IllegalArgumentException("The backoff '$name' is misconfigured: ${failure.message}", failure)
    }
}
