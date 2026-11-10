package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.aop.framework.AopProxyUtils
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
import org.springframework.core.GenericTypeResolver
import org.springframework.core.env.Environment
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.DEFAULT_BACKOFF_NAME
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory

private const val PROPERTIES_PREFIX = "semantic.resilience.backoff"
private const val DEFAULT_PROPERTY_NAME = "$PROPERTIES_PREFIX.$DEFAULT_BACKOFF_NAME"
private const val INSTANCES_PROPERTY_NAME = "$PROPERTIES_PREFIX.instances"
private const val JITTER_PROPERTY_NAME = "jitter"
private const val ROOT_NAME = ""
private const val DEFAULT_BACKOFF_TYPE = "exponential"
private const val BEAN_NAME_SUFFIX = "BackoffFactory"
private const val TYPE_SEPARATOR = '.'

internal class BackoffPropertiesRegistrar(
    environment: Environment,
    private val factories: Map<String, BackoffFactory<*>>,
) {

    private val sources = ConfigurationPropertySources.get(environment)

    private val placeholdersResolver = PropertySourcesPlaceholdersResolver(environment)

    private val binder = Binder(sources, placeholdersResolver)

    private val defaultName = ConfigurationPropertyName.of(DEFAULT_PROPERTY_NAME)

    private val instancesName = ConfigurationPropertyName.of(INSTANCES_PROPERTY_NAME)

    private val valueTarget = Bindable.of(String::class.java)

    private val jitterTarget = Bindable.of(Double::class.javaObjectType)

    private val beanNames = factories.keys
        .groupBy(::readUniformType)
        .mapValues { (type, candidates) -> requireSingleBeanName(type, candidates) }

    private val availableTypes = beanNames.values.map(::readType)

    private val configClasses = factories.mapValues { (beanName, factory) -> resolveConfigClass(beanName, factory) }

    fun registerBackoffs(registry: BackoffRegistry) {
        requireNoValue(instancesName)

        val instanceNames = readChildNames(instancesName)

        require(instanceNames.none(::isDefaultName)) {
            "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in the '$DEFAULT_BACKOFF_NAME' block"
        }

        registerBackoff(registry, defaultName)
        instanceNames.forEach { instanceName -> registerBackoff(registry, instanceName) }
    }

    // A bean name gives the type the way Spring Boot names health contributors: without the standard suffix.
    private fun readType(beanName: String) = when {
        beanName.endsWith(BEAN_NAME_SUFFIX, ignoreCase = true) -> beanName.dropLast(BEAN_NAME_SUFFIX.length)
        else -> beanName
    }

    private fun readTypeName(beanName: String) = beanName
        .let(::readType)
        .let { type -> ConfigurationPropertyName.adapt(type, TYPE_SEPARATOR) }
        .takeIf(::isSingleElement)
        ?.takeUnless(::isJitterName)
        ?: throw IllegalArgumentException(
            "The backoff factory bean '$beanName' gives the type '${readType(beanName)}': it must be a single " +
                "property name other than '$JITTER_PROPERTY_NAME', so give the bean a short name",
        )

    private fun readUniformType(beanName: String) = beanName
        .let(::readTypeName)
        .let(::readUniformName)

    private fun isSingleElement(propertyName: ConfigurationPropertyName) =
        propertyName.numberOfElements == 1 && !propertyName.isLastElementIndexed

    private fun readUniformName(propertyName: ConfigurationPropertyName) = propertyName.getLastElement(Form.UNIFORM)

    private fun readOriginalName(propertyName: ConfigurationPropertyName) = propertyName.getLastElement(Form.ORIGINAL)

    private fun isDefaultName(propertyName: ConfigurationPropertyName) =
        readUniformName(propertyName) == DEFAULT_BACKOFF_NAME

    private fun isJitterName(propertyName: ConfigurationPropertyName) =
        readUniformName(propertyName) == JITTER_PROPERTY_NAME

    private fun requireSingleBeanName(type: String, candidates: List<String>) = candidates.singleOrNull()
        ?: throw IllegalArgumentException("Several backoff factory beans give the type '$type': $candidates")

    private fun resolveConfigClass(beanName: String, factory: BackoffFactory<*>) = factory
        .let(AopProxyUtils::ultimateTargetClass)
        .let { factoryClass -> GenericTypeResolver.resolveTypeArgument(factoryClass, BackoffFactory::class.java) }
        ?: throw IllegalArgumentException(
            "The backoff factory bean '$beanName' must declare its config class as the type argument of BackoffFactory",
        )

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

        val defaultBeanName = findBeanName(defaultName) ?: getBeanName(DEFAULT_BACKOFF_TYPE)
        val beanName = findBeanName(blockName) ?: defaultBeanName
        val blockNames = listBlockNames(blockName, beanName, defaultBeanName)
        val backoff = factories
            .getValue(beanName)
            .let { factory -> createBackoff(beanName, factory, blockNames) }
        val jitter = bindJitter(blockName) ?: bindJitter(defaultName)

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun findBeanName(blockName: ConfigurationPropertyName): String? {
        val typeNames = readChildNames(blockName).filterNot(::isJitterName)
        val types = typeNames.map(::readOriginalName)
        val unknownTypes = typeNames
            .filterNot { typeName -> readUniformName(typeName) in beanNames }
            .map(::readOriginalName)

        require(unknownTypes.isEmpty()) { "Unknown backoff types $unknownTypes. Available types: $availableTypes" }
        require(types.size <= 1) { "Only one backoff type can be set: $types" }

        return typeNames
            .singleOrNull()
            ?.let(::readUniformName)
            ?.let(beanNames::getValue)
    }

    private fun getBeanName(type: String) = beanNames[type]
        ?: throw IllegalArgumentException("Unknown backoff type '$type'. Available types: $availableTypes")

    // Lowest-priority sources go first, so an override written in another form keeps the declared name.
    private fun readChildNames(propertyName: ConfigurationPropertyName) = sources
        .reversed()
        .filterIsInstance<IterableConfigurationPropertySource>()
        .flatMap { source -> source.filter(propertyName::isAncestorOf) }
        .map { name -> name.chop(propertyName.numberOfElements + 1) }
        .toSet()

    private fun listBlockNames(blockName: ConfigurationPropertyName, beanName: String, defaultBeanName: String) =
        when (beanName) {
            defaultBeanName -> listOf(blockName, defaultName)
            else -> listOf(blockName)
        }

    private fun bindJitter(blockName: ConfigurationPropertyName) = blockName
        .append(JITTER_PROPERTY_NAME)
        .let { jitterName -> binder.bind(jitterName, jitterTarget) }
        .orElse(null)

    private fun <C : Any> createBackoff(
        beanName: String,
        factory: BackoffFactory<C>,
        blockNames: List<ConfigurationPropertyName>,
    ): Backoff {
        val type = readType(beanName)
        val typeName = readTypeName(beanName)

        @Suppress("UNCHECKED_CAST")
        val configClass = configClasses.getValue(beanName) as Class<C>
        val configNames = blockNames.map { blockName -> blockName.append(typeName) }

        configNames.forEach(::requireNoValue)

        val config = try {
            bindConfig(configClass, configNames)
        } catch (failure: BindException) {
            throw createBindFailure(type, failure)
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

    private fun createBindFailure(type: String, failure: BindException) = describeBindFailure(type, failure)
        .let { message -> IllegalArgumentException(message, failure) }

    private fun describeBindFailure(type: String, failure: BindException) = failure.property
        ?.let { property -> "Invalid field '${property.name}' of the type '$type': ${failure.cause?.message}" }
        ?: "Invalid fields of the type '$type': ${failure.cause?.message}"

    private fun createMisconfiguredFailure(name: String, message: String?, cause: Throwable?) =
        IllegalArgumentException("The backoff '$name' is misconfigured: $message", cause)
}
