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

    private val valueTarget = Bindable.of(String::class.java)

    private val jitterTarget = Bindable.of(Double::class.javaObjectType)

    private val factories = factories
        .groupBy(::readUniformType)
        .mapValues { (type, candidates) -> requireSingleFactory(type, candidates) }

    private val availableTypes = this.factories.values.map { factory -> factory.type }

    fun registerBackoffs(registry: BackoffRegistry) {
        requireNoValue(instancesName)

        val instanceNames = readChildNames(instancesName)

        require(instanceNames.none(::isDefaultName)) {
            "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in the '$DEFAULT_BACKOFF_NAME' block"
        }

        registerBackoff(registry, defaultName)
        instanceNames.forEach { instanceName -> registerBackoff(registry, instanceName) }
    }

    private fun readUniformType(factory: BackoffFactory<*>) = factory.type
        .let(ConfigurationPropertyName::ofIfValid)
        ?.takeIf(::isSingleElement)
        ?.takeUnless(::isJitterName)
        ?.let(::readUniformName)
        ?: throw IllegalArgumentException(
            "The backoff factory ${factory.javaClass.name} declares the type '${factory.type}': " +
                "it must be a single property name other than '$JITTER_PROPERTY_NAME'",
        )

    private fun isSingleElement(propertyName: ConfigurationPropertyName) =
        propertyName.numberOfElements == 1 && !propertyName.isLastElementIndexed

    private fun readUniformName(propertyName: ConfigurationPropertyName) = propertyName.getLastElement(Form.UNIFORM)

    private fun readOriginalName(propertyName: ConfigurationPropertyName) = propertyName.getLastElement(Form.ORIGINAL)

    private fun isDefaultName(propertyName: ConfigurationPropertyName) =
        readUniformName(propertyName) == DEFAULT_BACKOFF_NAME

    private fun isJitterName(propertyName: ConfigurationPropertyName) =
        readUniformName(propertyName) == JITTER_PROPERTY_NAME

    private fun requireSingleFactory(type: String, candidates: List<BackoffFactory<*>>): BackoffFactory<*> {
        val names = candidates.map { factory -> factory.javaClass.name }

        return candidates.singleOrNull()
            ?: throw IllegalArgumentException("Several backoff factories declare the type '$type': $names")
    }

    private fun registerBackoff(registry: BackoffRegistry, blockName: ConfigurationPropertyName) {
        val name = readOriginalName(blockName)
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
        requireNoValue(blockName)

        val defaultFactory = findFactory(defaultName) ?: getFactory(EXPONENTIAL_BACKOFF_TYPE)
        val factory = findFactory(blockName) ?: defaultFactory
        val inheritedNames = listBlockNames(blockName, factory, defaultFactory)
        val backoff = createBackoff(factory, inheritedNames)
        val jitter = bindJitter(blockName) ?: bindJitter(defaultName)

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun findFactory(blockName: ConfigurationPropertyName): BackoffFactory<*>? {
        val typeNames = readChildNames(blockName).filterNot(::isJitterName)
        val types = typeNames.map(::readOriginalName)
        val unknownTypes = typeNames
            .filterNot { typeName -> readUniformName(typeName) in factories }
            .map(::readOriginalName)

        require(unknownTypes.isEmpty()) { "Unknown backoff types $unknownTypes. Available types: $availableTypes" }
        require(types.size <= 1) { "Only one backoff type can be set: $types" }

        return typeNames
            .singleOrNull()
            ?.let(::readUniformName)
            ?.let(factories::getValue)
    }

    private fun getFactory(type: String) = factories[type]
        ?: throw IllegalArgumentException("Unknown backoff type '$type'. Available types: $availableTypes")

    // Lowest-priority sources go first, so an override written in another form keeps the declared name.
    private fun readChildNames(propertyName: ConfigurationPropertyName) = sources
        .reversed()
        .filterIsInstance<IterableConfigurationPropertySource>()
        .flatMap { source -> source.filter(propertyName::isAncestorOf) }
        .map { name -> name.chop(propertyName.numberOfElements + 1) }
        .toSet()

    private fun listBlockNames(
        blockName: ConfigurationPropertyName,
        factory: BackoffFactory<*>,
        defaultFactory: BackoffFactory<*>,
    ) = when (factory) {
        defaultFactory -> listOf(blockName, defaultName)
        else -> listOf(blockName)
    }

    private fun bindJitter(blockName: ConfigurationPropertyName) = blockName
        .append(JITTER_PROPERTY_NAME)
        .let { jitterName -> binder.bind(jitterName, jitterTarget) }
        .orElse(null)

    private fun <C : Any> createBackoff(
        factory: BackoffFactory<C>,
        blockNames: List<ConfigurationPropertyName>,
    ): Backoff {
        val configNames = blockNames.map { blockName -> blockName.append(factory.type) }

        configNames.forEach(::requireNoValue)

        val config = try {
            bindConfig(factory.configClass, configNames)
        } catch (failure: BindException) {
            throw createBindFailure(factory, failure)
        }

        return factory.create(config)
    }

    private fun requireNoValue(propertyName: ConfigurationPropertyName) {
        val value = binder
            .bind(propertyName, valueTarget)
            .orElse(null)

        require(value.isNullOrBlank()) { "The property '$propertyName' takes nested settings, not the value '$value'" }
    }

    // One prefixed source per name, so a field of the instance takes priority over the same field of the default.
    private fun <C : Any> bindConfig(configClass: Class<C>, configNames: List<ConfigurationPropertyName>): C {
        val prefixes = configNames.map(ConfigurationPropertyName::toString)
        val configSources = prefixes.flatMap { prefix ->
            sources.map { source -> source.withPrefix(prefix) }
        }
        val target = Bindable.of(configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)

        return Binder(configSources, placeholdersResolver).bindOrCreate(ROOT_NAME, target, handler)
    }

    private fun createBindFailure(factory: BackoffFactory<*>, failure: BindException) = failure
        .let { bindFailure -> describeBindFailure(factory, bindFailure) }
        .let { message -> IllegalArgumentException(message, failure) }

    private fun describeBindFailure(factory: BackoffFactory<*>, failure: BindException) = failure.property
        ?.let { property ->
            "Invalid field '${property.name}' of the type '${factory.type}': ${failure.cause?.message}"
        }
        ?: "Invalid fields of the type '${factory.type}': ${failure.cause?.message}"

    private fun createMisconfiguredFailure(name: String, message: String?, cause: Throwable?) =
        IllegalArgumentException("The backoff '$name' is misconfigured: $message", cause)
}
