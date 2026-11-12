package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationPropertyException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleConfigurationsException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.UnknownConfigurationException
import kotlin.reflect.KClass

public class GenericConfigurationBinder<out T : Any>(
    private val resolver: Resolver<String, KClass<out T>>,
    private val decoder: PropertiesDecoder,
    private val defaultTypeName: String,
) : PolymorphicConfigurationBinder<T> {

    init {
        resolver.resolveOrNull(defaultTypeName)
            ?: throw IllegalArgumentException("The default type '$defaultTypeName' is unknown to the resolver")
    }

    @Throws(InvalidConfigurationException::class)
    override fun bind(prefix: String, properties: Map<String, String>): T {
        val typeName = findTypeName(prefix, properties)
            ?: defaultTypeName
        val type = resolveType(prefix, typeName)
        val typePrefix = prefix.appendSegment(typeName)

        checkHasNoUnboundProperties(prefix, properties)

        return properties
            .extractFields()
            .let { fields -> decode(fields, type, typePrefix) }
    }

    private fun findTypeName(prefix: String, properties: Map<String, String>) = properties.keys
        .map(String::extractTypeName)
        .filterNot(String::isEmpty)
        .distinctBy { typeName -> resolveType(prefix, typeName) }
        .toSet()
        .also { typeNames -> checkHasNoMultipleTypes(prefix, typeNames) }
        .singleOrNull()

    private fun resolveType(prefix: String, typeName: String) = resolver.resolveOrNull(typeName)
        ?: throw UnknownConfigurationException(prefix, typeName)

    private fun checkHasNoMultipleTypes(prefix: String, typeNames: Set<String>) {
        if (typeNames.size > 1) {
            throw MultipleConfigurationsException(prefix, typeNames)
        }
    }

    private fun checkHasNoUnboundProperties(prefix: String, properties: Map<String, String>) {
        val unboundKeys = properties
            .filter(Map.Entry<String, String>::isUnbound)
            .keys
            .map { key -> prefix.appendKey(key) }
            .map { key -> "'$key'" }

        if (unboundKeys.isNotEmpty()) {
            val cause = IllegalArgumentException("Unbound configuration properties: $unboundKeys")

            throw InvalidConfigurationPropertyException(prefix, cause)
        }
    }

    private fun decode(fields: Map<String, String>, type: KClass<out T>, typePrefix: String) = try {
        decoder.decode(fields, type)
    } catch (failure: Exception) {
        throw InvalidConfigurationPropertyException(typePrefix, failure)
    }
}
