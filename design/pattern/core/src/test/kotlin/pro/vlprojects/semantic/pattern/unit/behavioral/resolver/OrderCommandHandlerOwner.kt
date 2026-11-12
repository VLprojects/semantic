package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class OrderCommandHandlerOwner : CommandHandlerOwner<CreateOrder>() {

    inner class CreateOrderInnerHandler : Handler()
}
