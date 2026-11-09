package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.BindHandler
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffDefaultProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory

private const val ROOT_NAME = ""

internal class BackoffConfigurationService(
    private val defaults: BackoffDefaultProperties,
    factories: List<BackoffFactory<*>>,
) {

    private val factories = factories
        .groupBy { factory -> factory.type }
        .mapValues { (type, candidates) -> requireSingleFactory(type, candidates) }

    fun create(name: String, properties: BackoffProperties): Backoff = try {
        createBackoff(properties)
    } catch (failure: BindException) {
        throw IllegalArgumentException(createMisconfiguredMessage(name, failure.cause))
    } catch (failure: RuntimeException) {
        throw IllegalArgumentException(createMisconfiguredMessage(name, failure), failure)
    }

    private fun requireSingleFactory(type: String, candidates: List<BackoffFactory<*>>) = candidates.singleOrNull()
        ?: throw IllegalArgumentException(
            "Several backoff factories declare the type '$type': ${candidates.map { it.javaClass.name }}",
        )

    private fun createBackoff(properties: BackoffProperties): Backoff {
        val type = properties.type ?: defaults.type
        val backoff = findFactory(type).let { factory -> createBackoff(factory, findArgs(type, properties)) }
        val jitter = properties.jitter ?: defaults.jitter

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun findFactory(type: String) = factories[type]
        ?: throw IllegalArgumentException("Unknown backoff type '$type'. Available types: ${factories.keys}")

    private fun findArgs(type: String, properties: BackoffProperties) = when (type) {
        defaults.type -> listOf(properties.args, defaults.args)
        else -> listOf(properties.args)
    }

    private fun <C : Any> createBackoff(factory: BackoffFactory<C>, args: List<Map<String, String>>): Backoff {
        val config = bindConfig(factory.configClass, args)

        return factory.create(config)
    }

    // Each map stays a separate source: Binder treats initial-delay and initialDelay as one name, a merged map does not.
    private fun <C : Any> bindConfig(configClass: Class<C>, args: List<Map<String, String>>): C {
        val sources = args.map { values -> MapConfigurationPropertySource(values) }
        val target = Bindable.of(configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)

        return Binder(sources).bindOrCreate(ROOT_NAME, target, handler)
    }

    private fun createMisconfiguredMessage(name: String, cause: Throwable?) =
        "The backoff '$name' is misconfigured: ${cause?.message}"
}
