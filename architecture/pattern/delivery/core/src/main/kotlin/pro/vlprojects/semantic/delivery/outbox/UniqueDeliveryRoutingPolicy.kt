package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.NoUniqueOutboxDelivererException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotFoundException

public class UniqueDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): List<OutboxDeliverer<*>> = when (deliverers.size) {
        0 -> throw OutboxDelivererNotFoundException(destination)
        1 -> deliverers
        else -> throw NoUniqueOutboxDelivererException(destination)
    }
}
