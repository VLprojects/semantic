package pro.vlprojects.semantic.delivery.unit.outbox

import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEvent
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision

class OrderPlaced(order: Order) : AbstractEvent<Order, OrderId>(EventMeta.of<OrderPlaced>(), BaseRevision.of(order))
