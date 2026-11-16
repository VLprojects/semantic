package pro.vlprojects.semantic.pattern.behavioral.chain

/** A link of a chain: it handles a request or passes it on to its successor by [handleRequest]. */
public fun interface Handler<Request, Response> {

    public fun handleRequest(request: Request, successor: ChainOfResponsibility<Request, Response>): Response
}
