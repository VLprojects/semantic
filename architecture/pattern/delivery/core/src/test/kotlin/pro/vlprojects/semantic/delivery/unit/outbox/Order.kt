package pro.vlprojects.semantic.delivery.unit.outbox

import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot

class Order(identifier: OrderId) : AbstractAggregateRoot<Order, OrderId>() {

    init {
        this.identifier = identifier
    }
}
