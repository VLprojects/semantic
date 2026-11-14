package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

open class BatchingCommandHandler<C> : CommandHandler<C> {

    override fun handle(command: C) = Unit

    open inner class BatchHandler : BatchingCommandHandler<List<C>>()
}
