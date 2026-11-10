package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxRoutingException

public class OutboxDeliveryRouter(
    private val deliverers: List<OutboxDeliverer<*>>,
    private val policy: DeliveryRoutingPolicy = UniqueDeliveryRoutingPolicy(),
) {

    @Throws(OutboxRoutingException::class)
    public fun determine(destination: DeliveryDestination): OutboxDeliverer<DeliveryDestination> = deliverers
        .filter { deliverer -> deliverer.supports(destination) }
        .let { deliverers -> policy.select(destination, deliverers) }
        .let(::cast)

    // The selected deliverer either supports the destination or accepts any
    @Suppress("UNCHECKED_CAST")
    private fun cast(deliverer: OutboxDeliverer<*>) = deliverer as OutboxDeliverer<DeliveryDestination>
}
