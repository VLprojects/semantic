package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotResolvedException

public class LastDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): OutboxDeliverer<*> = deliverers.lastOrNull()
        ?: throw OutboxDelivererNotResolvedException(destination)
}
