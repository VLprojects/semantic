package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

public class TypeNameResolver<T : Any> private constructor(
    types: List<Pair<String, KClass<out T>>>,
) : Resolver<String, KClass<out T>> {

    public val typeNames: Set<String> = types
        .map(Pair<String, KClass<out T>>::first)
        .toSet()

    private val types = types
        .groupBy({ (typeName) -> typeName.toUniformName() }, Pair<String, KClass<out T>>::second)
        .mapValues { (typeName, namesakes) -> requireSingleType(typeName, namesakes) }

    override fun resolveAll(context: String): Collection<KClass<out T>> = context
        .toUniformName()
        .let(types::get)
        .let(::listOfNotNull)

    private fun requireSingleType(typeName: String, namesakes: List<KClass<out T>>) = namesakes
        .singleOrNull()
        ?: throw IllegalArgumentException("Several types are named '$typeName': $namesakes")

    public companion object Factory {

        public fun <T : Any> of(types: Map<String, KClass<out T>>): TypeNameResolver<T> = types
            .toList()
            .let(::TypeNameResolver)

        public fun <T : Any> of(
            baseType: KClass<T>,
            subtypes: Collection<KClass<out T>>,
        ): TypeNameResolver<T> = subtypes
            .map { subtype -> subtype.readTypeName(baseType) to subtype }
            .let(::TypeNameResolver)
    }
}

private fun KClass<*>.readTypeName(baseType: KClass<*>) = this.java.simpleName
    .removeSuffix(baseType.java.simpleName)
    .ifEmpty { this.java.simpleName }
    .toDashedForm()

private fun String.toDashedForm() = this
    .mapIndexed { index, char ->
        when {
            index > 0 && char.isUpperCase() -> "-${char.lowercaseChar()}"
            else -> char.lowercase()
        }
    }
    .joinToString("")

private fun String.toUniformName() = this
    .filter(Char::isLetterOrDigit)
    .lowercase()
