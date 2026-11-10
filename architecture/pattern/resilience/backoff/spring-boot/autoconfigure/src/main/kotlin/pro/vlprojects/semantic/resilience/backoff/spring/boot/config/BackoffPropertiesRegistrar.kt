package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.BindHandler
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.core.env.Environment
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.DEFAULT_BACKOFF_NAME
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.EXPONENTIAL_BACKOFF_TYPE

private const val PROPERTIES_PREFIX = "semantic.resilience.backoff"
private const val DEFAULT_PROPERTY_NAME = "$PROPERTIES_PREFIX.$DEFAULT_BACKOFF_NAME"
private const val INSTANCES_PROPERTY_NAME = "$PROPERTIES_PREFIX.instances"
private const val JITTER_PROPERTY_NAME = "jitter"
private const val ROOT_NAME = ""

internal class BackoffPropertiesRegistrar(
    environment: Environment,
    factories: List<BackoffFactory<*>>,
) {

    private val sources = ConfigurationPropertySources.get(environment)

    private val placeholdersResolver = PropertySourcesPlaceholdersResolver(environment)

    private val binder = Binder(sources, placeholdersResolver)

    private val factories = factories
        .groupBy { factory -> factory.type }
        .mapValues { (type, candidates) -> requireSingleFactory(type, candidates) }

    fun registerBackoffs(registry: BackoffRegistry) {
        val names = readKeys(INSTANCES_PROPERTY_NAME)

        require(DEFAULT_BACKOFF_NAME !in names) {
            "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in the '$DEFAULT_BACKOFF_NAME' block"
        }

        registerBackoff(registry, DEFAULT_BACKOFF_NAME, DEFAULT_PROPERTY_NAME)
        names.forEach { name -> registerBackoff(registry, name, "$INSTANCES_PROPERTY_NAME[$name]") }
    }

    private fun requireSingleFactory(type: String, candidates: List<BackoffFactory<*>>): BackoffFactory<*> {
        val names = candidates.map { factory -> factory.javaClass.name }

        return candidates.singleOrNull()
            ?: throw IllegalArgumentException("Several backoff factories declare the type '$type': $names")
    }

    private fun registerBackoff(registry: BackoffRegistry, name: String, propertyName: String) {
        val backoff = try {
            createBackoff(propertyName)
        } catch (failure: BindException) {
            throw createMisconfiguredFailure(name, "${failure.message}: ${failure.cause?.message}", failure.cause)
        } catch (failure: RuntimeException) {
            throw createMisconfiguredFailure(name, failure.message, failure)
        }

        registry.register(name, backoff)
    }

    private fun createBackoff(propertyName: String): Backoff {
        val defaultType = findType(DEFAULT_PROPERTY_NAME) ?: EXPONENTIAL_BACKOFF_TYPE
        val type = findType(propertyName) ?: defaultType
        val configNames = findConfigNames(propertyName, type, defaultType)
        val backoff = findFactory(type).let { factory -> createBackoff(factory, configNames) }
        val jitter = bindJitter(propertyName) ?: bindJitter(DEFAULT_PROPERTY_NAME)

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun findType(propertyName: String): String? {
        val types = readKeys(propertyName) - JITTER_PROPERTY_NAME
        val unknownTypes = types - factories.keys

        require(unknownTypes.isEmpty()) { "Unknown backoff types $unknownTypes. Available types: ${factories.keys}" }
        require(types.size <= 1) { "Only one backoff type can be set: $types" }

        return types.singleOrNull()
    }

    private fun readKeys(propertyName: String) = binder
        .bindOrCreate(propertyName, Bindable.mapOf(String::class.java, Any::class.java))
        .keys

    private fun findConfigNames(propertyName: String, type: String, defaultType: String) = when (type) {
        defaultType -> listOf("$propertyName.$type", "$DEFAULT_PROPERTY_NAME.$type")
        else -> listOf("$propertyName.$type")
    }

    private fun findFactory(type: String) = factories[type]
        ?: throw IllegalArgumentException("Unknown backoff type '$type'. Available types: ${factories.keys}")

    private fun bindJitter(propertyName: String) = binder
        .bind("$propertyName.$JITTER_PROPERTY_NAME", Double::class.javaObjectType)
        .orElse(null)

    private fun <C : Any> createBackoff(factory: BackoffFactory<C>, configNames: List<String>): Backoff {
        val config = bindConfig(factory.configClass, configNames)

        return factory.create(config)
    }

    // One prefixed source per name, so a field of the instance takes priority over the same field of the default.
    private fun <C : Any> bindConfig(configClass: Class<C>, configNames: List<String>): C {
        val configSources = configNames.flatMap { configName ->
            sources.map { source -> source.withPrefix(configName) }
        }
        val target = Bindable.of(configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)

        return Binder(configSources, placeholdersResolver).bindOrCreate(ROOT_NAME, target, handler)
    }

    private fun createMisconfiguredFailure(name: String, message: String?, cause: Throwable?) =
        IllegalArgumentException("The backoff '$name' is misconfigured: $message", cause)
}
