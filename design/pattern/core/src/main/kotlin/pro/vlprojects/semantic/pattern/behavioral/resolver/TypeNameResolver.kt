package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

public class TypeNameResolver<T : Any> private constructor(
    types: Map<String, KClass<out T>>,
) : Resolver<String, KClass<out T>> {

    private val types = types.mapKeys { (typeName) -> typeName.toUniformForm() }

    override fun resolveAll(context: String): Collection<KClass<out T>> = context
        .toUniformForm()
        .let(types::get)
        .let(::listOfNotNull)

    public companion object Factory {

        public fun <T : Any> of(types: Map<String, KClass<out T>>): TypeNameResolver<T> = types
            .requireNotBlankTypeNames()
            .requireDistinctTypeNames()
            .let(::TypeNameResolver)

        public fun <T : Any> of(
            baseType: KClass<T>,
            subtypes: Collection<KClass<out T>>,
        ): TypeNameResolver<T> = subtypes
            .toSet()
            .requireNamedClasses()
            .requireDistinctTypeNames(baseType)
            .associateBy { subtype -> subtype.buildTypeName(baseType) }
            .let(::of)
    }
}
