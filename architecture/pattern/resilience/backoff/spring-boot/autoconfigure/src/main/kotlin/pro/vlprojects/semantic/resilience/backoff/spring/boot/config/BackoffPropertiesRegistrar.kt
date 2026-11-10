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
private const val DEFAULT_TYPE_NAME = "exponential"
private const val KEY_SEPARATOR = '.'

internal class BackoffPropertiesRegistrar(
    private val properties: BackoffProperties,
    private val types: BackoffTypeRegistry,
) {

    private val defaultTypeName = ConfigurationPropertyName.of(DEFAULT_TYPE_NAME)

    private val defaultType by lazy {
        findType(DEFAULT_PROPERTY_NAME, properties.default)
            ?: types.find(defaultTypeName)
            ?: throw IllegalStateException("No backoff factory bean gives the type '$DEFAULT_TYPE_NAME'")
    }

    fun registerBackoffs(registry: BackoffRegistry) {
        requireNoInstanceNamedDefault()
        registry.register(DEFAULT_BACKOFF_NAME, createBackoff(DEFAULT_PROPERTY_NAME, properties.default))
        properties.instances.forEach { (name, settings) ->
            registry.register(name, createBackoff("$INSTANCES_PROPERTY_NAME[$name]", settings))
        }
    }

    private fun requireNoInstanceNamedDefault() {
        if (DEFAULT_BACKOFF_NAME in properties.instances) {
            throw InvalidConfigurationPropertyValueException(
                INSTANCES_PROPERTY_NAME,
                DEFAULT_BACKOFF_NAME,
                "The backoff name '$DEFAULT_BACKOFF_NAME' is reserved: set it in '$DEFAULT_PROPERTY_NAME'",
            )
        }
    }

    private fun createBackoff(blockName: String, settings: Map<String, String>): Backoff {
        val type = findType(blockName, settings) ?: defaultType
        val binder = createBinder(blockName, settings)

        return try {
            createBackoff(blockName, type, binder)
        } catch (failure: IllegalArgumentException) {
            throw InvalidConfigurationPropertyValueException(blockName, settings, failure.message)
        }
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
            ?.let { typeName -> findType(blockName, typeName) }
    }

    private fun findType(blockName: String, typeName: ConfigurationPropertyName) = types.find(typeName)
        ?: throw InvalidConfigurationPropertyValueException(
            blockName,
            typeName.getLastElement(Form.ORIGINAL),
            "Unknown backoff type. Available types: ${types.names}",
        )

    // The default block goes second, so a field of the instance takes priority over the same field of the default.
    private fun createBinder(blockName: String, settings: Map<String, String>) = listOf(settings, properties.default)
        .map { blockSettings -> blockSettings.filterValues(String::isNotBlank) }
        .map(::MapConfigurationPropertySource)
        .map { source -> source.withPrefix(blockName) }
        .let(::Binder)

    private fun <C : Any> createBackoff(blockName: String, type: BackoffType<C>, binder: Binder): Backoff {
        val configName = ConfigurationPropertyName
            .of(blockName)
            .append(type.name)
        val target = Bindable.of(type.configClass)
        val handler = NoUnboundElementsBindHandler(BindHandler.DEFAULT)
        val config = binder.bindOrCreate(configName, target, handler)

        return type.factory.create(config)
    }
}
