package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CreateOrderCommandModule : CommandModule<CreateOrder>() {

    inner class CreateOrderHandlers : Handlers() {

        inner class CreateOrderNestedInnerHandler : Handler()
    }
}
