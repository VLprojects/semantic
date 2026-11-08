package pro.vlprojects.semantic.resilience.backoff.spring.boot

import pro.vlprojects.semantic.resilience.backoff.Backoff

internal class BackoffsFactory(private val properties: BackoffProperties) {

    private val defaults = ResolvedBackoffSettings().overrideWith(properties.default)

    fun create(beans: Map<String, Backoff>) = properties.instances
        .also(::checkDefaultIsNotAnInstance)
        .minus(beans.keys)
        .mapValues(::createBackoff)
        .plus(beans)
        .let(::assembleBackoffs)

    private fun checkDefaultIsNotAnInstance(instances: Map<String, BackoffSettings>) = require(
        DEFAULT_NAME !in instances,
    ) {
        "The backoff '$DEFAULT_NAME' is not an instance: set it in the '$DEFAULT_NAME' block"
    }

    private fun createBackoff(instance: Map.Entry<String, BackoffSettings>) = rethrowWithName(instance.key) {
        defaults
            .overrideWith(instance.value)
            .also { resolved -> resolved.checkAllFieldsAreUsed(instance.value) }
            .createBackoff()
    }

    private fun assembleBackoffs(named: Map<String, Backoff>): Backoffs {
        val default = named[DEFAULT_NAME] ?: rethrowWithName(DEFAULT_NAME, defaults::createBackoff)
        return Backoffs(default, named + (DEFAULT_NAME to default))
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
