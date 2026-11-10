package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.MultipleOutboxDeliverersException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotResolvedException

public class UniqueDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<*>>,
    ): OutboxDeliverer<*> = when (deliverers.size) {
        0 -> throw OutboxDelivererNotResolvedException(destination)
        1 -> deliverers.single()
        else -> throw MultipleOutboxDeliverersException(destination)
    }
}
