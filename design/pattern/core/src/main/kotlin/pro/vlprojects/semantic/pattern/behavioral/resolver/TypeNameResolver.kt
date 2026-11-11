package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

private val CAMEL_CASE_BOUNDARY = Regex("(?<=.)(?=\\p{Lu})")

public class TypeNameResolver<T : Any> private constructor(
    types: Map<String, KClass<out T>>,
) : Resolver<String, KClass<out T>> {

    public val typeNames: Set<String> = types.keys

    private val typesByUniformName = types.mapKeys { (typeName) -> typeName.toUniformForm() }

    init {
        require(typesByUniformName.size == typeNames.size) { "Several types have one name: $typeNames" }
    }

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
            .associateBy { subtype -> subtype.readTypeName(baseType) }
            .also { types -> requireNamePerSubtype(types, subtypes) }
            .let(::TypeNameResolver)

        private fun requireNamePerSubtype(types: Map<String, KClass<*>>, subtypes: Collection<KClass<*>>) {
            require(types.size == subtypes.size) { "Several subtypes have one name: $subtypes" }
        }
    }
}

private fun KClass<*>.readTypeName(baseType: KClass<*>) = this.java.simpleName
    .removeSuffix(baseType.java.simpleName)
    .ifEmpty { this.java.simpleName }
    .toDashedForm()

private fun String.toDashedForm() = this
    .replace(CAMEL_CASE_BOUNDARY, "-")
    .lowercase()

private fun String.toUniformForm() = this
    .filter(Char::isLetterOrDigit)
    .lowercase()
