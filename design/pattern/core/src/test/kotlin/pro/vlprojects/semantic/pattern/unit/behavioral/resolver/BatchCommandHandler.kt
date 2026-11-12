package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class BatchCommandHandler : CommandHandler<List<CreateOrder>> {

    override fun handle(command: List<CreateOrder>) = Unit
}
