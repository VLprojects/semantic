package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.destination.Destination
import kotlin.reflect.KClass

public interface OutboxDeliverer<D : Destination> {

    public val destinationType: KClass<D>

    public fun deliver(outbox: Outbox, destination: D)
}
