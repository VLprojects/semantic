package pro.vlprojects.semantic.pattern.behavioral.chain

internal class DefaultChainOfResponsibility<Request, Response>(
    private val handler: Handler<Request, Response>,
    private val successor: ChainOfResponsibility<Request, Response>,
) : ChainOfResponsibility<Request, Response> {

    override fun handleRequest(request: Request) = handler.handleRequest(request, successor)
}
