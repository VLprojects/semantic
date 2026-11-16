package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CreateOrderOuterCommandHandler : OuterCommandHandler<CreateOrder>() {

    inner class CreateOrderInnerCommandHandler : InnerCommandHandler()
}
