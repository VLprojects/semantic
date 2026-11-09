package pro.vlprojects.semantic.delivery.outbox

public class FallbackDeliveryRoutingPolicy(private val fallback: DeliveryRoute<*>) : DeliveryRoutingPolicy {

    override fun select(
        destination: DeliveryDestination,
        routes: List<DeliveryRoute<*>>,
    ): List<DeliveryRoute<*>> = routes.ifEmpty { listOf(fallback) }
}
