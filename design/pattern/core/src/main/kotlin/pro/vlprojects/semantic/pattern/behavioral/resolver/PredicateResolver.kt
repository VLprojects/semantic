package pro.vlprojects.semantic.pattern.behavioral.resolver

public class PredicateResolver<in C, T : Any>(
    candidates: Collection<T>,
    private val predicate: (T, C) -> Boolean,
) : Resolver<C, T> {

    private val candidates = candidates.toList()

    override fun resolveAll(context: C): Collection<T> = candidates
        .filter { candidate -> predicate(candidate, context) }
}
