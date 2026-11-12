package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class ConfirmOrderHandler : OrderCommandHandler<ConfirmOrder> {

    override fun handle(command: ConfirmOrder) = Unit
}
