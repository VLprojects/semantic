package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxRoutingException

public fun interface DeliveryRoutingPolicy {

    @Throws(OutboxRoutingException::class)
    public fun select(destination: DeliveryDestination, deliverers: List<OutboxDeliverer<*>>): OutboxDeliverer<*>
}
