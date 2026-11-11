package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

public class TypeNameResolver<T : Any> private constructor(
    private val types: Map<String, KClass<out T>>,
) : Resolver<String, KClass<out T>> {

    override fun resolveAll(context: String): Collection<KClass<out T>> = context
        .toUniformTypeName()
        .let(types::get)
        .let(::listOfNotNull)

    public companion object Factory {

        public fun <T : Any> of(types: Map<String, KClass<out T>>): TypeNameResolver<T> = types.keys
            .groupBy(String::toUniformTypeName)
            .also(::checkHasNoBlankTypeName)
            .also(::checkHasNoDuplicateTypeName)
            .mapValues { (_, group) -> group.single().let(types::getValue) }
            .let(::TypeNameResolver)

        public fun <T : Any> of(
            baseType: KClass<T>,
            subtypes: Collection<KClass<out T>>,
        ): TypeNameResolver<T> = subtypes
            .map(KClass<out T>::java)
            .toSet()
            .also(::checkHasNoAnonymousClass)
            .groupBy { subtype -> subtype.extractTypeName(excludeBaseType = baseType.java).toUniformTypeName() }
            .also(::checkHasNoBlankTypeName)
            .also(::checkHasNoDuplicateTypeName)
            .mapValues { (_, group) -> group.single().kotlin }
            .let(::TypeNameResolver)
    }
}
