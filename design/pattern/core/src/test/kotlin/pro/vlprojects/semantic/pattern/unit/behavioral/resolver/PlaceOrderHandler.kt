package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class PlaceOrderHandler : OrderCommand.Handler<PlaceOrder, Long> {

    override fun handle(command: PlaceOrder) = 1L
}
