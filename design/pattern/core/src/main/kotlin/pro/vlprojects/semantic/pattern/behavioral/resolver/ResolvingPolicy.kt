package pro.vlprojects.semantic.pattern.behavioral.resolver

public fun interface ResolvingPolicy<T : Any> {

    public fun select(candidates: Collection<T>): T

    public companion object Factory {

        public fun <T : Any> first(): ResolvingPolicy<T> = ResolvingPolicy(Collection<T>::first)

        public fun <T : Any> last(): ResolvingPolicy<T> = ResolvingPolicy(Collection<T>::last)
    }
}
