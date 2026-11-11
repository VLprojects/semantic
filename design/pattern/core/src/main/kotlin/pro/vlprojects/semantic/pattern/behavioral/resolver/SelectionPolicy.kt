package pro.vlprojects.semantic.pattern.behavioral.resolver

public fun interface SelectionPolicy<in C, T : Any> {

    public fun select(context: C, candidates: Candidates<T>): T
}
