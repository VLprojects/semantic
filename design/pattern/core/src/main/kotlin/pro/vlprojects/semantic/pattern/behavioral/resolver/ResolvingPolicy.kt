package pro.vlprojects.semantic.pattern.behavioral.resolver

public fun interface ResolvingPolicy<T : Any> {

    public fun select(candidates: Collection<T>): T
}
