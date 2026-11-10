package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public class OutboxDeliveryRouter(
    private val deserializer: DeliveryDestinationDeserializer,
    private val deliverers: List<OutboxDeliverer<*>>,
    private val policy: DeliveryRoutingPolicy = UniqueDeliveryRoutingPolicy(),
) {

    @Throws(OutboxDeliveryException::class)
    public fun deliver(outbox: Outbox) {
        val destination = deserializer.deserialize(outbox.destination)

        deliverers
            .filter { it.supports(destination) }
            .let { policy.select(destination, it) }
            .map(::cast)
            .forEach { it.send(outbox, destination) }
    }

    // A selected deliverer either supports the destination or accepts any
    @Suppress("UNCHECKED_CAST")
    private fun cast(deliverer: OutboxDeliverer<*>) = deliverer as OutboxDeliverer<DeliveryDestination>
}
