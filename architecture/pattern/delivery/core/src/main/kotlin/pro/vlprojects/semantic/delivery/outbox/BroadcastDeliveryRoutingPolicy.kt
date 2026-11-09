package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotFoundException

public class BroadcastDeliveryRoutingPolicy : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        routes: List<DeliveryRoute<*>>,
    ): List<DeliveryRoute<*>> = routes.ifEmpty { throw OutboxDelivererNotFoundException(destination) }
}
