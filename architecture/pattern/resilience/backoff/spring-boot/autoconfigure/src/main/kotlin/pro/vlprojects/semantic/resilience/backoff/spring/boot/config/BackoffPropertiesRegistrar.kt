package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.boot.context.properties.bind.BindHandler
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler
import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import org.springframework.boot.context.properties.source.ConfigurationPropertyName.Form
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.DEFAULT_BACKOFF_NAME
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffType
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffTypeRegistry

private const val DEFAULT_PROPERTY_NAME = "$BACKOFF_PROPERTIES_PREFIX.$DEFAULT_BACKOFF_NAME"
private const val INSTANCES_PROPERTY_NAME = "$BACKOFF_PROPERTIES_PREFIX.instances"
private const val FALLBACK_TYPE_NAME = "exponential"
private const val KEY_SEPARATOR = '.'

internal class BackoffPropertiesRegistrar(
    private val properties: BackoffProperties,
    private val types: BackoffTypeRegistry,
) {

    private val fallbackTypeName = ConfigurationPropertyName.of(FALLBACK_TYPE_NAME)

    private val defaultType = findType(DEFAULT_PROPERTY_NAME, properties.default) ?: getFallbackType()

    fun registerBackoffs(registry: BackoffRegistry) {
        requireNoInstanceNamedDefault()
        registerBackoff(registry, DEFAULT_BACKOFF_NAME, DEFAULT_PROPERTY_NAME, properties.default)
        properties.instances.forEach { (name, settings) ->
            registerBackoff(registry, name, "$INSTANCES_PROPERTY_NAME[$name]", settings)
        }
    }

    private fun getFallbackType() = types.find(fallbackTypeName)
        ?: throw IllegalStateException("No backoff factory bean gives the type '$FALLBACK_TYPE_NAME'")

    private fun requireNoInstanceNamedDefault() {
        if (DEFAULT_BACKOFF_NAME in properties.instances) {
            throw InvalidConfigurationPropertyValueException(
                INSTANCES_PROPERTY_NAME,
                DEFAULT_BACKOFF_NAME,
                "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in '$DEFAULT_PROPERTY_NAME'",
            )
        }
    }

    private fun registerBackoff(
        registry: BackoffRegistry,
        name: String,
        blockName: String,
        settings: Map<String, String>,
    ) {
        val type = findType(blockName, settings) ?: defaultType
        val backoff = createBackoff(blockName, settings, type)

        registry.register(name, backoff)
    }

    private fun findType(blockName: String, settings: Map<String, String>): BackoffType<*>? {
        val typeNames = settings.keys
            .map { key -> ConfigurationPropertyName.adapt(key, KEY_SEPARATOR) }
            .map { name -> name.chop(1) }
            .toSet()

        if (typeNames.size > 1) {
            throw InvalidConfigurationPropertyValueException(blockName, typeNames, "Only one backoff type can be set")
        }

        return typeNames
            .singleOrNull()
            ?.let { typeName -> getType(blockName, typeName) }
    }

    private fun getType(blockName: String, typeName: ConfigurationPropertyName): BackoffType<*> {
        val type = typeName.getLastElement(Form.ORIGINAL)

        return types.find(typeName)
            ?: throw InvalidConfigurationPropertyValueException(
                blockName,
                type,
                "Unknown backoff type. Available types: ${types.names}",
            )
    }

    private fun <C : Any> createBackoff(
        blockName: String,
        settings: Map<String, String>,
        type: BackoffType<C>,
    ): Backoff {
        val config = bindConfig(blockName, settings, type)

        return try {
            type.factory.create(config)
        } catch (failure: IllegalArgumentException) {
            throw createInvalidValueFailure(blockName, settings, failure)
        }
    }

    private fun <C : Any> bindConfig(blockName: String, settings: Map<String, String>, type: BackoffType<C>): C {
        val configName = ConfigurationPropertyName
            .of(blockName)
            .append(type.name)
        val target = Bindable.of(type.configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)

        return createBinder(blockName, settings).bindOrCreate(configName, target, handler)
    }

    // The default block goes second, so a field of the instance takes priority over the same field of the default.
    private fun createBinder(blockName: String, settings: Map<String, String>) = listOf(settings, properties.default)
        .map { blockSettings -> blockSettings.filterValues(String::isNotBlank) }
        .map { blockSettings -> blockSettings.mapKeys { (key) -> "$blockName$KEY_SEPARATOR$key" } }
        .map(::MapConfigurationPropertySource)
        .let(::Binder)

    private fun createInvalidValueFailure(blockName: String, settings: Map<String, String>, cause: Throwable) =
        InvalidConfigurationPropertyValueException(blockName, settings, cause.message)
            .apply { initCause(cause) }
}
