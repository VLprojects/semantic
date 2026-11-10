package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.BindHandler
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler
import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import org.springframework.boot.context.properties.source.ConfigurationPropertyName.Form
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.boot.context.properties.source.IterableConfigurationPropertySource
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

    private val defaultName = ConfigurationPropertyName.of(DEFAULT_PROPERTY_NAME)

    private val instancesName = ConfigurationPropertyName.of(INSTANCES_PROPERTY_NAME)

    private val factories = factories
        .groupBy { factory -> readType(factory) }
        .mapValues { (type, candidates) -> requireSingleFactory(type, candidates) }

    private val availableTypes = this.factories.values.map { factory -> factory.type }

    fun registerBackoffs(registry: BackoffRegistry) {
        val instanceNames = readChildNames(instancesName)

        require(instanceNames.none(::isDefaultName)) {
            "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in the '$DEFAULT_BACKOFF_NAME' block"
        }

        registerBackoff(registry, DEFAULT_BACKOFF_NAME, defaultName)
        instanceNames.forEach { instanceName ->
            registerBackoff(registry, instanceName.getLastElement(Form.ORIGINAL), instanceName)
        }
    }

    private fun readType(factory: BackoffFactory<*>) = factory.type
        .let(ConfigurationPropertyName::of)
        .let(::readType)

    private fun readType(propertyName: ConfigurationPropertyName) = propertyName.getLastElement(Form.UNIFORM)

    private fun requireSingleFactory(type: String, candidates: List<BackoffFactory<*>>): BackoffFactory<*> {
        val names = candidates.map { factory -> factory.javaClass.name }

        return candidates.singleOrNull()
            ?: throw IllegalArgumentException("Several backoff factories declare the type '$type': $names")
    }

    private fun isDefaultName(instanceName: ConfigurationPropertyName) = readType(instanceName) == DEFAULT_BACKOFF_NAME

    private fun registerBackoff(registry: BackoffRegistry, name: String, blockName: ConfigurationPropertyName) {
        val backoff = try {
            createBackoff(blockName)
        } catch (failure: BindException) {
            throw createMisconfiguredFailure(name, "${failure.message}: ${failure.cause?.message}", failure.cause)
        } catch (failure: RuntimeException) {
            throw createMisconfiguredFailure(name, failure.message, failure)
        }

        registry.register(name, backoff)
    }

    private fun createBackoff(blockName: ConfigurationPropertyName): Backoff {
        val defaultType = findTypeName(defaultName)?.let(::readType) ?: EXPONENTIAL_BACKOFF_TYPE
        val type = findTypeName(blockName)?.let(::readType) ?: defaultType
        val configNames = findConfigNames(blockName, type, defaultType)
        val backoff = findFactory(type).let { factory -> createBackoff(factory, configNames) }
        val jitter = bindJitter(blockName) ?: bindJitter(defaultName)

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun findTypeName(blockName: ConfigurationPropertyName): ConfigurationPropertyName? {
        val typeNames = readChildNames(blockName).filterNot(::isJitterName)
        val unknownTypes = typeNames
            .filterNot { typeName -> readType(typeName) in factories }
            .map { typeName -> typeName.getLastElement(Form.ORIGINAL) }

        require(unknownTypes.isEmpty()) { "Unknown backoff types $unknownTypes. Available types: $availableTypes" }
        require(typeNames.size <= 1) { "Only one backoff type can be set: $typeNames" }

        return typeNames.singleOrNull()
    }

    // Children are read from the sources, as MapBinder does, so that keys in any relaxed form meet.
    private fun readChildNames(propertyName: ConfigurationPropertyName) = sources
        .filterIsInstance<IterableConfigurationPropertySource>()
        .flatMap { source -> source.filter(propertyName::isAncestorOf) }
        .map { name -> name.chop(propertyName.numberOfElements + 1) }
        .toSet()

    private fun isJitterName(propertyName: ConfigurationPropertyName) = readType(propertyName) == JITTER_PROPERTY_NAME

    private fun findConfigNames(blockName: ConfigurationPropertyName, type: String, defaultType: String) =
        when (type) {
            defaultType -> listOf(blockName.append(type), defaultName.append(type))
            else -> listOf(blockName.append(type))
        }

    private fun findFactory(type: String) = factories[type]
        ?: throw IllegalArgumentException("Unknown backoff type '$type'. Available types: $availableTypes")

    private fun bindJitter(blockName: ConfigurationPropertyName) = binder
        .bind(blockName.append(JITTER_PROPERTY_NAME), Bindable.of(Double::class.javaObjectType))
        .orElse(null)

    private fun <C : Any> createBackoff(
        factory: BackoffFactory<C>,
        configNames: List<ConfigurationPropertyName>,
    ): Backoff {
        configNames.forEach(::requireNoValue)

        val config = bindConfig(factory.configClass, configNames)

        return factory.create(config)
    }

    private fun requireNoValue(configName: ConfigurationPropertyName) {
        val value = binder
            .bind(configName, Bindable.of(String::class.java))
            .orElse(null)

        require(value.isNullOrBlank()) { "The type '${readType(configName)}' takes fields, not the value '$value'" }
    }

    // One prefixed source per name, so a field of the instance takes priority over the same field of the default.
    private fun <C : Any> bindConfig(configClass: Class<C>, configNames: List<ConfigurationPropertyName>): C {
        val configSources = configNames.flatMap { configName ->
            sources.map { source -> source.withPrefix(configName.toString()) }
        }
        val target = Bindable.of(configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)

        return Binder(configSources, placeholdersResolver).bindOrCreate(ROOT_NAME, target, handler)
    }

    private fun createMisconfiguredFailure(name: String, message: String?, cause: Throwable?) =
        IllegalArgumentException("The backoff '$name' is misconfigured: $message", cause)
}
