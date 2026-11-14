package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CreateOrderArrayHandler : ArrayCommandHandler<CreateOrder>() {

    override fun handle(command: Array<CreateOrder>) = Unit
}
