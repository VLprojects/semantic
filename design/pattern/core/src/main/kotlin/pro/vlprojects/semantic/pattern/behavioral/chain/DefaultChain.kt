package pro.vlprojects.semantic.pattern.behavioral.chain

internal class DefaultChain<T, R>(
    private val handler: Handler<T, R>,
    private val next: Chain<T, R>,
) : Chain<T, R> {

    override fun proceed(request: T) = handler.handle(request, next)
}
