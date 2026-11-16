package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CreateOrderCommandHandlerOwner : CommandHandlerOwner<CreateOrder>() {

    inner class CreateOrderInnerHandler : Handler()

    inner class CreateOrderForwardingHandler : ForwardingHandler()
}
