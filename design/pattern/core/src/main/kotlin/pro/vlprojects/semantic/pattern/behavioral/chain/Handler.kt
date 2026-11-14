package pro.vlprojects.semantic.pattern.behavioral.chain

/** A link of a chain: it handles a request or passes it on by [Chain.proceed] of the rest. */
public fun interface Handler<T, R> {

    public fun handle(request: T, chain: Chain<T, R>): R
}
