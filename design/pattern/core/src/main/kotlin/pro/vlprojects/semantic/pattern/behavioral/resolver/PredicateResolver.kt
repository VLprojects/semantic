package pro.vlprojects.semantic.pattern.behavioral.resolver

public class PredicateResolver<in C, T : Any>(
    values: List<T>,
    private val predicate: (T, C) -> Boolean,
) : Resolver<C, T> {

    private val values = values.toList()

    override fun resolveAll(context: C): Collection<T> = values
        .filter { value -> predicate(value, context) }
}
