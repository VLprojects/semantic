package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public class DeliveryRoute<D : DeliveryDestination> private constructor(
    private val deliverer: OutboxDeliverer<D>,
    private val destination: D,
) {

    @Throws(OutboxDeliveryException::class)
    public fun send(outbox: Outbox) {
        deliverer.send(outbox, destination)
    }

    public companion object Factory {

        public fun <D : DeliveryDestination> of(
            deliverer: OutboxDeliverer<D>,
            destination: D,
        ): DeliveryRoute<D> = DeliveryRoute(deliverer, destination)
    }
}
