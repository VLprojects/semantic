package pro.vlprojects.semantic.pattern.behavioral.chain

/** A chain, or the rest of it that a [Handler] passes a request on to. */
public fun interface Chain<in T, out R> {

    public fun proceed(request: T): R

    public companion object Factory {

        // O(n) links built once; a call allocates nothing and goes as deep as the handler that handles it.
        public fun <T, R> of(handlers: List<Handler<T, R>>, endOfChain: Chain<T, R>): Chain<T, R> = handlers
            .foldRight(endOfChain, ::DefaultChain)
    }
}
