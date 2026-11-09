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

    private val factories = factories.associateBy { factory -> factory.type }

    fun create(name: String, properties: BackoffProperties): Backoff = try {
        createBackoff(properties)
    } catch (failure: IllegalArgumentException) {
        throw createMisconfiguredFailure(name, failure)
    } catch (failure: BindException) {
        throw createMisconfiguredFailure(name, failure)
    }

    private fun createBackoff(properties: BackoffProperties): Backoff {
        val type = properties.type ?: defaults.type
        val args = if (type == defaults.type) defaults.args + properties.args else properties.args
        val backoff = createBackoff(findFactory(type), args)
        val jitter = properties.jitter ?: defaults.jitter

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun findFactory(type: String) = factories[type]
        ?: throw IllegalArgumentException("Unknown backoff type '$type'. Available types: ${factories.keys}")

    private fun <C : Any> createBackoff(factory: BackoffFactory<C>, args: Map<String, String>): Backoff {
        val config = bindConfig(factory.configClass, args)

        return factory.create(config)
    }

    private fun <C : Any> bindConfig(configClass: Class<C>, args: Map<String, String>): C {
        val source = MapConfigurationPropertySource(args)
        val target = Bindable.of(configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)

        return Binder(source).bindOrCreate(ROOT_NAME, target, handler)
    }

    private fun createMisconfiguredFailure(name: String, cause: Exception) =
        IllegalArgumentException("The backoff '$name' is misconfigured: ${cause.message}", cause)
}
