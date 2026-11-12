package pro.vlprojects.semantic.pattern.enterprise.plugin.base

import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleResolvedException
import pro.vlprojects.semantic.pattern.enterprise.plugin.PolymorphicConfigurationBinder
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleTypesException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.TypeNotFoundException
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

    @Throws(TypeNotFoundException::class, MultipleTypesException::class, MultipleResolvedException::class)
    final override fun bind(name: String, properties: Map<String, String>, defaults: Map<String, String>): T {
        val typeName = findTypeName(name, properties)
            ?: findTypeName(name, defaults)
            ?: defaultTypeName
        val type = resolveType(name, typeName)
        val sources = listOf(properties, defaults).map { block -> selectFields(block, type) }

        return doBind("$name$KEY_SEPARATOR$typeName", type, sources)
    }

    /**
     * Binds [sources], the fields of the block before those of the defaults, to an object of [type] at [name].
     */
    protected abstract fun doBind(name: String, type: KClass<out T>, sources: List<Map<String, String>>): T

    private fun findTypeName(name: String, block: Map<String, String>) = block.keys
        .map(String::extractTypeName)
        .distinct()
        .groupBy { typeName -> resolveType(name, typeName) }
        .values
        .map(List<String>::first)
        .also { typeNames -> checkHasSingleType(name, typeNames) }
        .singleOrNull()

    private fun resolveType(name: String, typeName: String) = resolver.resolveOrNull(typeName)
        ?: throw TypeNotFoundException(name, typeName)

    private fun selectFields(block: Map<String, String>, type: KClass<out T>) = block
        .filterKeys { key -> isFieldOf(key, type) }
        .mapKeys { (key) -> key.extractFieldName() }
        .filterKeys(String::isNotEmpty)

    private fun isFieldOf(key: String, type: KClass<out T>) = key.extractTypeName().let(resolver::resolveOrNull) == type
}
