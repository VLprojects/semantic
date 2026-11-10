package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public class OutboxDeliveryRouter(
    private val deliverers: List<OutboxDeliverer<*>>,
    private val policy: DeliveryRoutingPolicy = UniqueDeliveryRoutingPolicy(),
) {

    @Throws(OutboxDeliveryException::class)
    public fun determine(destination: DeliveryDestination): List<OutboxDeliverer<DeliveryDestination>> = deliverers
        .filter { it.supports(destination) }
        .let { policy.select(destination, it) }
        .map(::cast)

    // A selected deliverer either supports the destination or accepts any
    @Suppress("UNCHECKED_CAST")
    private fun cast(deliverer: OutboxDeliverer<*>) = deliverer as OutboxDeliverer<DeliveryDestination>
}
