package pro.vlprojects.semantic.pattern.behavioral.resolver

import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleResolvedException
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.NotResolvedException

public fun interface Resolver<in C, T : Any> {

    public fun resolveAll(context: C): Collection<T>

    public fun resolveOrNull(context: C): T? = resolveOrNull(context) { candidates ->
        throw MultipleResolvedException(context, candidates)
    }

    public fun resolveOrNull(context: C, policy: ResolvingPolicy<T>): T? {
        val candidates = resolveAll(context)

        return when (candidates.size) {
            0 -> null
            1 -> candidates.single()
            else -> policy.select(candidates)
        }
    }

    public fun resolve(context: C): T = resolveOrNull(context)
        ?: throw NotResolvedException(context)

    public fun resolve(context: C, policy: ResolvingPolicy<T>): T = resolveOrNull(context, policy)
        ?: throw NotResolvedException(context)
}
