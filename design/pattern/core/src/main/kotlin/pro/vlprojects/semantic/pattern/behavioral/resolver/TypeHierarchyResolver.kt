package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

public class TypeHierarchyResolver<in C : Any, T : Any> private constructor(
    private val candidatesByType: Map<Class<*>, List<T>>,
) : Resolver<C, T> {

    override fun resolveAll(context: C): Collection<T> = context.javaClass
        .collectTypeHierarchy()
        .mapNotNull(candidatesByType::get)
        .flatten()

    public companion object Factory {

        public fun <C : Any, T : Any> of(
            candidates: Collection<T>,
            typeSelector: (T) -> KClass<*>,
        ): TypeHierarchyResolver<C, T> = candidates
            .groupBy { candidate -> typeSelector(candidate).javaObjectType }
            .let(::TypeHierarchyResolver)
    }
}
