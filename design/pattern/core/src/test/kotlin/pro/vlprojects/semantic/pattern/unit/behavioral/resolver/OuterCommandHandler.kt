package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

open class OuterCommandHandler<C> : CommandHandler<C> {

    override fun handle(command: C) = Unit

    open inner class InnerCommandHandler : OuterCommandHandler<C>()
}
