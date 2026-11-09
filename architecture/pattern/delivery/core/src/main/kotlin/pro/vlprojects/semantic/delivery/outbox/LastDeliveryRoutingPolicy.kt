package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotResolvedException

public class LastDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): List<OutboxDeliverer<*>> = deliverers
        .takeLast(1)
        .ifEmpty { throw OutboxDelivererNotResolvedException(destination) }
}
