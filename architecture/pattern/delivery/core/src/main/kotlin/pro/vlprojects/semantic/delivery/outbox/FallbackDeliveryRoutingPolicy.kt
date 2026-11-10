package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.MultipleOutboxDeliverersException

public class FallbackDeliveryRoutingPolicy(
    private val fallback: OutboxDeliverer<DeliveryDestination>,
) : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): OutboxDeliverer<*> = when (deliverers.size) {
        0 -> fallback
        1 -> deliverers.single()
        else -> throw MultipleOutboxDeliverersException(destination)
    }
}
