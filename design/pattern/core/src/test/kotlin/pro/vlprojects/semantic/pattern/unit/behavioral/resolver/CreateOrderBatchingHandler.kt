package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class CreateOrderBatchingHandler : BatchingCommandHandler<CreateOrder>() {

    inner class CreateOrderBatchHandler : BatchHandler()
}
