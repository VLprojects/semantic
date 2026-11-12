package pro.vlprojects.semantic.pattern.enterprise.plugin.base

import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleResolvedException
import pro.vlprojects.semantic.pattern.enterprise.plugin.PolymorphicConfigurationBinder
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationPropertyException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.UnknownConfigurationException
import kotlin.reflect.KClass

/**
 * Finds the type of a block and selects its fields in a final [bind], leaving the binding of the fields
 * to [doBind]; [resolver] must know [defaultTypeName].
 */
public abstract class AbstractPolymorphicConfigurationBinder<T : Any>(
    private val resolver: Resolver<String, KClass<out T>>,
    private val defaultTypeName: String,
) : PolymorphicConfigurationBinder<T> {

    init {
        checkHasDefaultType(resolver, defaultTypeName)
    }

    @Throws(InvalidConfigurationException::class, MultipleResolvedException::class)
    final override fun bind(prefix: String, properties: Map<String, String>, defaults: Map<String, String>): T {
        val typeName = findTypeName(prefix, properties)
            ?: findTypeName(prefix, defaults)
            ?: defaultTypeName
        val type = resolveType(prefix, typeName)
        val sources = listOf(properties, defaults).map { block -> selectFields(block, type) }

        return doBind("$prefix$KEY_SEPARATOR$typeName", type, sources)
    }

    /**
     * Binds [sources], the fields of the block before those of the defaults, to an object of [type] under [prefix].
     */
    @Throws(InvalidConfigurationPropertyException::class)
    protected abstract fun doBind(prefix: String, type: KClass<out T>, sources: List<Map<String, String>>): T

    private fun findTypeName(prefix: String, block: Map<String, String>) = block.keys
        .map(String::extractTypeName)
        .distinct()
        .groupBy { typeName -> resolveType(prefix, typeName) }
        .values
        .map(List<String>::first)
        .also { typeNames -> checkHasSingleType(prefix, typeNames) }
        .singleOrNull()

    private fun resolveType(prefix: String, typeName: String) = resolver.resolveOrNull(typeName)
        ?: throw UnknownConfigurationException(prefix, typeName)

    private fun selectFields(block: Map<String, String>, type: KClass<out T>) = block
        .filterKeys { key -> isFieldOf(key, type) }
        .mapKeys { (key) -> key.extractFieldName() }
        .filterKeys(String::isNotEmpty)

    private fun isFieldOf(key: String, type: KClass<out T>) = key.extractTypeName().let(resolver::resolveOrNull) == type
}
