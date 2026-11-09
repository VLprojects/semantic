package pro.vlprojects.semantic.delivery.outbox.dispatch

import pro.vlprojects.semantic.delivery.outbox.Outbox

public class OutboxDeliveryRouter(
    private val resolver: DeliveryDestinationResolver,
    private val deliverers: List<OutboxDeliverer<*>>,
) {

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
