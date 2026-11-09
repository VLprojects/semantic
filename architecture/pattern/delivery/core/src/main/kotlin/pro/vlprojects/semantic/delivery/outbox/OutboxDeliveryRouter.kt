package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public class OutboxDeliveryRouter(
    private val resolver: DeliveryDestinationResolver,
    private val deliverers: List<OutboxDeliverer<*>>,
) {

    @Throws(OutboxDeliveryException::class)
    public fun route(outbox: Outbox) {
        val destination = resolver.resolve(outbox.context)

        findDeliverer(destination).deliver(outbox, destination)
    }

    // supports() guards the cast
    @Suppress("UNCHECKED_CAST")
    private fun findDeliverer(destination: DeliveryDestination) = deliverers
        .firstOrNull { it.supports(destination) } as OutboxDeliverer<DeliveryDestination>?
        ?: error("No deliverer for the destination: $destination")
}
