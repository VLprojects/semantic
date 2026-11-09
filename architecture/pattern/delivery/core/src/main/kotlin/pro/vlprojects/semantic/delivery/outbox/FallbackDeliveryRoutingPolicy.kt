package pro.vlprojects.semantic.delivery.outbox

public class FallbackDeliveryRoutingPolicy(
    private val fallback: OutboxDeliverer<DeliveryDestination>,
) : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): List<OutboxDeliverer<*>> = deliverers.ifEmpty { listOf(fallback) }
}
