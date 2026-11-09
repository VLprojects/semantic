package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.NoUniqueOutboxDelivererException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotFoundException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public class OutboxDeliveryRouter(
    private val deserializer: DeliveryDestinationDeserializer,
    private val deliverers: List<OutboxDeliverer<*>>,
) {

    @Throws(OutboxDeliveryException::class)
    public fun route(outbox: Outbox) {
        val destination = deserializer.deserialize(outbox.destination)

        resolveDeliverer(destination).send(outbox, destination)
    }

    // supports() guards the cast
    @Suppress("UNCHECKED_CAST")
    private fun resolveDeliverer(destination: DeliveryDestination) = deliverers
        .filter { it.supports(destination) }
        .let { supporting ->
            when (supporting.size) {
                0 -> throw OutboxDelivererNotFoundException(destination)
                1 -> supporting.single() as OutboxDeliverer<DeliveryDestination>
                else -> throw NoUniqueOutboxDelivererException(destination)
            }
        }
}
