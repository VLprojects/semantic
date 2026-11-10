package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotResolvedException

public class FirstDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): OutboxDeliverer<*> = deliverers.firstOrNull()
        ?: throw OutboxDelivererNotResolvedException(destination)
}
