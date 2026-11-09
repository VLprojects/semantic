package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.destination.Destination
import pro.vlprojects.semantic.delivery.outbox.destination.DestinationResolver
import kotlin.reflect.cast

public class OutboxRouter(
    private val resolver: DestinationResolver,
    deliverers: List<OutboxDeliverer<*>>,
) {

    private val deliverers = deliverers.associateBy { it.destinationType }

    init {
        require(this.deliverers.size == deliverers.size) { "Each destination type must have one deliverer" }
    }

    public fun route(outbox: Outbox) {
        val destination = resolver.resolve(outbox.context)

        findDeliverer(destination).deliverTo(outbox, destination)
    }

    private fun findDeliverer(destination: Destination) = deliverers[destination::class]
        ?: error("No deliverer for the destination: ${destination::class.qualifiedName}")

    private fun <D : Destination> OutboxDeliverer<D>.deliverTo(outbox: Outbox, destination: Destination) = destination
        .let(this.destinationType::cast)
        .let { this.deliver(outbox, it) }
}
