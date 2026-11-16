package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class DualCreateOrderHandler :
    AbstractCommandHandler<CreateOrder>(),
    OrderCommandHandler<CreateOrder> {

    override fun handle(command: CreateOrder) = Unit
}
