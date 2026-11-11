package pro.vlprojects.semantic.pattern.behavioral.resolver

import pro.vlprojects.semantic.pattern.behavioral.resolver.error.CandidateNotFoundException
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleCandidatesException

public fun interface Resolver<in C, T : Any> {

    public fun resolveAll(context: C): Collection<T>

    public fun resolveOrNull(context: C): T? = resolveOrNull(context) { candidates ->
        throw MultipleCandidatesException(candidates)
    }

    public fun resolveOrNull(context: C, policy: ResolvingPolicy<T>): T? = context
        .let(::resolveAll)
        .let { candidates ->
            when (candidates.size) {
                0 -> null
                1 -> candidates.single()
                else -> policy.select(candidates)
            }
        }

    public fun resolve(context: C): T = resolveOrNull(context)
        ?: throw CandidateNotFoundException(context)

    public fun resolve(context: C, policy: ResolvingPolicy<T>): T = resolveOrNull(context, policy)
        ?: throw CandidateNotFoundException(context)
}
