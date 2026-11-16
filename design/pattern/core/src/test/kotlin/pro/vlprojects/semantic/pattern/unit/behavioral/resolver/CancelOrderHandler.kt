package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CancelOrderHandler : AbstractCommandHandler<CancelOrder>() {

    override fun handle(command: CancelOrder) = Unit
}
