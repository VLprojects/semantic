package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public fun interface DeliveryRoutingPolicy {

    @Throws(OutboxDeliveryException::class)
    public fun select(destination: DeliveryDestination, routes: List<DeliveryRoute<*>>): List<DeliveryRoute<*>>
}
