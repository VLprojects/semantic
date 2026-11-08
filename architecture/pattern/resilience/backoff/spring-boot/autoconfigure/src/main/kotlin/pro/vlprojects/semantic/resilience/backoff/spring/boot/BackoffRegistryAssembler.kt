package pro.vlprojects.semantic.resilience.backoff.spring.boot

import pro.vlprojects.semantic.resilience.backoff.Backoff

internal class BackoffRegistryAssembler(private val properties: BackoffProperties) {

    private val defaults = ResolvedBackoffProperties().overrideWith(properties.default)

    fun assemble(beans: Map<String, Backoff>) = properties.instances
        .also(::checkDefaultIsNotAnInstance)
        .minus(beans.keys)
        .mapValues(::createBackoff)
        .plus(beans)
        .let(::createRegistry)

    private fun checkDefaultIsNotAnInstance(instances: Map<String, BackoffInstanceProperties>) = require(
        DEFAULT_NAME !in instances,
    ) {
        "The backoff '$DEFAULT_NAME' is not an instance: set it in the '$DEFAULT_NAME' block"
    }

    private fun createBackoff(instance: Map.Entry<String, BackoffInstanceProperties>) = rethrowWithName(instance.key) {
        defaults
            .overrideWith(instance.value)
            .also { resolved -> resolved.checkAllFieldsAreUsed(instance.value) }
            .createBackoff()
    }

    private fun createRegistry(named: Map<String, Backoff>): BackoffRegistry {
        val default = named[DEFAULT_NAME] ?: rethrowWithName(DEFAULT_NAME, defaults::createBackoff)

        return BackoffRegistry(default, named + (DEFAULT_NAME to default))
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
