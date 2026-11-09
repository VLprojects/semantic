package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public class OutboxDeliveryRouter(
    private val deserializer: DeliveryDestinationDeserializer,
    private val deliverers: List<OutboxDeliverer<*>>,
    private val policy: DeliveryRoutingPolicy = UniqueDeliveryRoutingPolicy(),
) {

    @Throws(OutboxDeliveryException::class)
    public fun route(outbox: Outbox) {
        val destination = deserializer.deserialize(outbox.destination)

        resolveRoutes(destination)
            .let { policy.select(destination, it) }
            .forEach { it.send(outbox) }
    }

    // supports() guards the cast
    @Suppress("UNCHECKED_CAST")
    private fun resolveRoutes(destination: DeliveryDestination) = deliverers
        .filter { it.supports(destination) }
        .map { DeliveryRoute.of(it as OutboxDeliverer<DeliveryDestination>, destination) }
}
