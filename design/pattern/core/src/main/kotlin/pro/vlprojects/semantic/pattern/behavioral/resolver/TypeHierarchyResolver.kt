package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

public class TypeHierarchyResolver<in C : Any, T : Any> private constructor(
    private val valuesByType: Map<Class<*>, T>,
) : Resolver<C, T> {

    override fun resolveAll(context: C): Collection<T> = context.javaClass
        .collectTypeHierarchy()
        .mapNotNull(valuesByType::get)

    public companion object Factory {

        public fun <C : Any, T : Any> of(
            valuesByType: Map<out KClass<*>, T>,
        ): TypeHierarchyResolver<C, T> = valuesByType
            .mapKeys { (type) -> type.javaObjectType }
            .let(::TypeHierarchyResolver)
    }
}
