package pro.vlprojects.semantic.pattern.behavioral.resolver

public fun interface Resolver<in C, out T : Any> {

    public fun resolve(context: C): Candidates<T>
}
