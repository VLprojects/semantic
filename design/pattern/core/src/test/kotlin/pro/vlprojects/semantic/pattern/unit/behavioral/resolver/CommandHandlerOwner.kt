package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

open class CommandHandlerOwner<C> {

    open inner class Handler : CommandHandler<C> {

        override fun handle(command: C) = Unit
    }

    open inner class ForwardingHandler : Handler()
}
