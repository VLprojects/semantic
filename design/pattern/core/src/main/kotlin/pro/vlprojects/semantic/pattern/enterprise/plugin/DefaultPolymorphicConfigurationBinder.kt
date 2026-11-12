package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationPropertyException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.UnknownConfigurationException
import kotlin.reflect.KClass

public class DefaultPolymorphicConfigurationBinder<out T : Any>(
    private val resolver: Resolver<String, KClass<out T>>,
    private val decoder: PropertiesDecoder,
    private val defaultTypeName: String,
) : PolymorphicConfigurationBinder<T> {

    private val defaultType = resolver.resolveOrNull(defaultTypeName)
        ?: throw IllegalArgumentException("The default type '$defaultTypeName' is unknown to the resolver")

    @Throws(InvalidConfigurationException::class)
    override fun bind(prefix: String, properties: Map<String, String>): T {
        val (type, typeName) = findType(prefix, properties)
            ?: (defaultType to defaultTypeName)

        checkHasNoUnboundProperties(prefix, properties)

        val typePrefix = prefix.appendSegment(typeName)

        return properties
            .extractFields()
            .let { fields -> decode(typePrefix, fields, type) }
    }

    private fun findType(prefix: String, properties: Map<String, String>) = properties.keys
        .map(String::extractTypeName)
        .filterNot(String::isEmpty)
        .distinct()
        .groupBy { typeName -> resolveType(prefix, typeName) }
        .mapValues { (_, typeNames) -> typeNames.first() }
        .also { typeNamesByType -> checkHasNoMultipleTypes(prefix, typeNamesByType) }
        .entries
        .singleOrNull()
        ?.toPair()

    private fun resolveType(prefix: String, typeName: String) = resolver.resolveOrNull(typeName)
        ?: throw UnknownConfigurationException(prefix, typeName)

    private fun decode(typePrefix: String, fields: Map<String, String>, type: KClass<out T>) = try {
        decoder.decode(fields, type)
    } catch (failure: Exception) {
        throw InvalidConfigurationPropertyException(typePrefix, failure)
    }
}
