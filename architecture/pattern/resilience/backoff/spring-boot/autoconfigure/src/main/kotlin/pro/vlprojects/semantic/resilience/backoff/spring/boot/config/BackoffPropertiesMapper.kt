package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties

internal class BackoffPropertiesMapper(private val properties: BackoffConfigurationProperties) {

    fun mapToRegistry(beans: Map<String, Backoff>) = properties.instances
        .also(::checkDefaultIsNotAnInstance)
        .minus(beans.keys)
        .mapValues(::createBackoff)
        .plus(beans)
        .let(::createRegistry)

    private fun checkDefaultIsNotAnInstance(instances: Map<String, BackoffProperties>) = require(
        DEFAULT_NAME !in instances,
    ) {
        "The backoff '$DEFAULT_NAME' is not an instance: set it in the '$DEFAULT_NAME' block"
    }

    private fun createBackoff(instance: Map.Entry<String, BackoffProperties>) = rethrowWithName(instance.key) {
        properties.default.createBackoff(instance.value)
    }

    private fun createRegistry(backoffs: Map<String, Backoff>): BackoffRegistry {
        val default = backoffs[DEFAULT_NAME] ?: rethrowWithName(DEFAULT_NAME, properties.default::createBackoff)

        return BackoffRegistry(default, backoffs + (DEFAULT_NAME to default))
    }

    private fun rethrowWithName(name: String, createBackoff: () -> Backoff) = try {
        createBackoff()
    } catch (failure: IllegalArgumentException) {
        throw IllegalArgumentException("The backoff '$name' is misconfigured: ${failure.message}", failure)
    }

    private companion object {

        const val DEFAULT_NAME = "default"
    }
}
