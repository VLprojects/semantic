package pro.vlprojects.semantic.pattern.behavioral.resolver

import pro.vlprojects.semantic.format.toKebabCase
import kotlin.reflect.KClass

private val NAME_SEPARATORS = setOf('-', '_')

public class TypeNameResolver<T : Any> private constructor(
    types: Map<String, KClass<out T>>,
) : Resolver<String, KClass<out T>> {

    public val typeNames: Set<String> = types.keys.toSet()

    private val typesByUniformName = types.entries
        .onEach { (typeName) -> requireTypeName(typeName) }
        .groupBy { (typeName) -> typeName.toUniformForm() }
        .mapValues { (_, namedTypes) -> namedTypes.requireSingleType() }

    override fun resolveAll(context: String): Collection<KClass<out T>> = context
        .toUniformForm()
        .let(typesByUniformName::get)
        .let(::listOfNotNull)

    public companion object Factory {

        public fun <T : Any> of(types: Map<String, KClass<out T>>): TypeNameResolver<T> = TypeNameResolver(types)

        public fun <T : Any> of(
            baseType: KClass<T>,
            subtypes: Collection<KClass<out T>>,
        ): TypeNameResolver<T> = subtypes
            .toSet()
            .onEach(::requireClassName)
            .groupBy { subtype -> subtype.readTypeName(baseType) }
            .mapValues { (typeName, namedSubtypes) -> namedSubtypes.requireSingleSubtype(typeName) }
            .let(::TypeNameResolver)
    }
}

private fun requireTypeName(typeName: String) {
    require(typeName.toUniformForm().isNotEmpty()) {
        "A type name is empty or has dashes and underscores only: '$typeName'"
    }
}

private fun requireClassName(subtype: KClass<*>) {
    require(subtype.java.simpleName.isNotEmpty()) { "A class without a name has no type name: ${subtype.java.name}" }
}

private fun <T : Any> List<Map.Entry<String, KClass<out T>>>.requireSingleType(): KClass<out T> {
    require(this.size == 1) {
        this.joinToString(
            prefix = "Type names coincide regardless of case, dashes and underscores: ",
        ) { (typeName, type) -> "'$typeName' of ${type.java.name}" }
    }

    return this.single().value
}

private fun <T : Any> List<KClass<out T>>.requireSingleSubtype(typeName: String): KClass<out T> {
    require(this.size == 1) {
        this.joinToString(prefix = "Several subtypes have the name '$typeName': ") { subtype -> subtype.java.name }
    }

    return this.single()
}

private fun KClass<*>.readTypeName(baseType: KClass<*>) = this.java.simpleName
    .removeSuffix(baseType.java.simpleName)
    .ifEmpty { this.java.simpleName }
    .toKebabCase()

private fun String.toUniformForm() = this
    .lowercase()
    .filterNot { char -> char in NAME_SEPARATORS }
