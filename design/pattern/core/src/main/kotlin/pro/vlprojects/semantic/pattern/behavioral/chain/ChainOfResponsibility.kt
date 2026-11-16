package pro.vlprojects.semantic.pattern.behavioral.chain

/** A chain of handlers, or the rest of it that a [Handler] passes a request on to as its successor. */
public fun interface ChainOfResponsibility<in Request, out Response> {

    public fun handleRequest(request: Request): Response

    public companion object Factory {

        // O(n) links built once; a call allocates nothing and goes as deep as the handler that handles it.
        public fun <Request, Response> of(
            handlers: List<Handler<Request, Response>>,
            endOfChain: ChainOfResponsibility<Request, Response>,
        ): ChainOfResponsibility<Request, Response> = handlers
            .foldRight(endOfChain, ::DefaultChainOfResponsibility)
    }
}
