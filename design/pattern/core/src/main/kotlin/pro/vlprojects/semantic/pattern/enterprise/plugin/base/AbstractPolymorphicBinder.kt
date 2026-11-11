package pro.vlprojects.semantic.pattern.enterprise.plugin.base

import pro.vlprojects.semantic.pattern.enterprise.plugin.PolymorphicBinder
import pro.vlprojects.semantic.pattern.enterprise.plugin.TypeNameResolver
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleTypesException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.TypeNotFoundException
import kotlin.reflect.KClass

private const val KEY_SEPARATOR = '.'

public abstract class AbstractPolymorphicBinder<T : Any>(
    private val resolver: TypeNameResolver<T>,
    private val defaultTypeName: String,
) : PolymorphicBinder<T> {

    @Throws(TypeNotFoundException::class, MultipleTypesException::class)
    final override fun bind(name: String, properties: Map<String, String>, defaults: Map<String, String>): T {
        val typeName = findTypeName(name, properties)
            ?: findTypeName(name, defaults)
            ?: defaultTypeName
        val type = resolveType(name, typeName)
        val sources = listOf(properties, defaults)
            .map { block -> selectFields(block, type) }

        return doBind("$name$KEY_SEPARATOR$typeName", type, sources)
    }

    protected abstract fun doBind(name: String, type: KClass<out T>, sources: List<Map<String, String>>): T

    private fun findTypeName(name: String, block: Map<String, String>) = block.keys
        .map(String::readTypeName)
        .distinct()
        .groupBy { typeName -> resolveType(name, typeName) }
        .values
        .map(List<String>::first)
        .let { typeNames -> requireSingleType(name, typeNames) }

    private fun requireSingleType(name: String, typeNames: List<String>) = when {
        typeNames.size > 1 -> throw MultipleTypesException(name, typeNames.toSet())
        else -> typeNames.firstOrNull()
    }

    private fun resolveType(name: String, typeName: String) = resolver.find(typeName)
        ?: throw TypeNotFoundException(name, typeName, resolver.typeNames)

    private fun selectFields(block: Map<String, String>, type: KClass<out T>) = block
        .filterKeys { key -> isFieldOf(key, type) }
        .mapKeys { (key) -> key.readFieldName() }
        .filterKeys(String::isNotEmpty)

    private fun isFieldOf(key: String, type: KClass<out T>) = key
        .readTypeName()
        .let(resolver::find) == type
}

private fun String.readTypeName() = this.substringBefore(KEY_SEPARATOR)

private fun String.readFieldName() = this.substringAfter(KEY_SEPARATOR, missingDelimiterValue = "")
