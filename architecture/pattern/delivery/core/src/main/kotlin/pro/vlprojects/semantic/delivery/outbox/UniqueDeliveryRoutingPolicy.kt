package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.NoUniqueOutboxDelivererException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotFoundException

public class UniqueDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        routes: List<DeliveryRoute<*>>,
    ): List<DeliveryRoute<*>> = when (routes.size) {
        0 -> throw OutboxDelivererNotFoundException(destination)
        1 -> routes
        else -> throw NoUniqueOutboxDelivererException(destination)
    }
}
