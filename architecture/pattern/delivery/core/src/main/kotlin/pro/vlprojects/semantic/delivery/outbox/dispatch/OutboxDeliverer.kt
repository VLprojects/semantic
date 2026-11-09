package pro.vlprojects.semantic.delivery.outbox.dispatch

import pro.vlprojects.semantic.delivery.outbox.Outbox

public interface OutboxDeliverer<D : DeliveryDestination> {

    public fun supports(destination: DeliveryDestination): Boolean

    public fun deliver(outbox: Outbox, destination: D)
}
