package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CreateOrderListArrayHandler : CommandHandler<Array<List<CreateOrder>>> {

    override fun handle(command: Array<List<CreateOrder>>) = Unit
}
