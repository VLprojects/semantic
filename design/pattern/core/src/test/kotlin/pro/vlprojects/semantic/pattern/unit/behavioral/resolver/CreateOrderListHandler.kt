package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CreateOrderListHandler : CommandHandler<List<CreateOrder>> {

    override fun handle(command: List<CreateOrder>) = Unit
}
