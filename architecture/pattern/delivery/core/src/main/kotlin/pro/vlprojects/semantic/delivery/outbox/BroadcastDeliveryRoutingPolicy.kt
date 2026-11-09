package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotFoundException

public class BroadcastDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): List<OutboxDeliverer<*>> = deliverers.ifEmpty { throw OutboxDelivererNotFoundException(destination) }
}
