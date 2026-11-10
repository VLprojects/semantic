package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.BindHandler
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.DEFAULT_BACKOFF_NAME
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory

private const val ROOT_NAME = ""

internal class BackoffPropertiesRegistrar(
    private val properties: BackoffConfigurationProperties,
    factories: List<BackoffFactory<*>>,
) {

    private val factories = factories
        .groupBy { factory -> factory.type }
        .mapValues { (type, candidates) -> requireSingleFactory(type, candidates) }

    fun registerBackoffs(registry: BackoffRegistry) {
        require(DEFAULT_BACKOFF_NAME !in properties.instances) {
            "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in the '$DEFAULT_BACKOFF_NAME' block"
        }

        properties.instances.forEach { (name, instance) -> registerBackoff(registry, name, instance) }
        registerBackoff(registry, DEFAULT_BACKOFF_NAME, BackoffProperties())
    }

    private fun requireSingleFactory(type: String, candidates: List<BackoffFactory<*>>): BackoffFactory<*> {
        val names = candidates.map { factory -> factory.javaClass.name }

        return candidates.singleOrNull()
            ?: throw IllegalArgumentException("Several backoff factories declare the type '$type': $names")
    }

    private fun registerBackoff(registry: BackoffRegistry, name: String, instance: BackoffProperties) {
        val backoff = try {
            createBackoff(instance)
        } catch (failure: BindException) {
            throw createMisconfiguredFailure(name, "${failure.message}: ${failure.cause?.message}", failure.cause)
        } catch (failure: RuntimeException) {
            throw createMisconfiguredFailure(name, failure.message, failure)
        }

        registry.register(name, backoff)
    }

    private fun createBackoff(instance: BackoffProperties): Backoff {
        val type = instance.type ?: properties.default.type
        val args = findArgs(type, instance)
        val backoff = findFactory(type).let { factory -> createBackoff(factory, args) }
        val jitter = instance.jitter ?: properties.default.jitter

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun findFactory(type: String) = factories[type]
        ?: throw IllegalArgumentException("Unknown backoff type '$type'. Available types: ${factories.keys}")

    private fun findArgs(type: String, instance: BackoffProperties) = when (type) {
        properties.default.type -> listOf(instance.args, properties.default.args)
        else -> listOf(instance.args)
    }

    private fun <C : Any> createBackoff(factory: BackoffFactory<C>, args: List<Map<String, String>>): Backoff {
        val config = bindConfig(factory.configClass, args)

        return factory.create(config)
    }

    // Separate sources: Binder treats initial-delay and initialDelay as one name, a merged map does not.
    private fun <C : Any> bindConfig(configClass: Class<C>, args: List<Map<String, String>>): C {
        val sources = args.map { values -> MapConfigurationPropertySource(values) }
        val target = Bindable.of(configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)

        return Binder(sources).bindOrCreate(ROOT_NAME, target, handler)
    }

    private fun createMisconfiguredFailure(name: String, message: String?, cause: Throwable?) =
        IllegalArgumentException("The backoff '$name' is misconfigured: $message", cause)
}
