package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException

public interface OutboxDeliverer<D : DeliveryDestination> {

    public fun supports(destination: DeliveryDestination): Boolean

    @Throws(OutboxDeliveryException::class)
    public fun deliver(outbox: Outbox, destination: D)
}
