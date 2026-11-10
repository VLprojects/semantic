package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException

public class OutboxDispatcher(
    private val service: OutboxService,
    private val deserializer: DeliveryDestinationDeserializer,
    private val router: OutboxDeliveryRouter,
    private val config: OutboxProperties,
) {

    public fun dispatch(): Int = service
        .getScheduledWithLock(config.batchSize)
        .onEach(::process)
        .size

    private fun process(outbox: Outbox) {
        val deliverers = resolveDeliverers(outbox)
        val delivery = deliver(outbox, deliverers)

        saveResult(outbox, delivery)
    }

    private fun resolveDeliverers(outbox: Outbox) = runCatching {
        val destination = deserializer.deserialize(outbox.destination)

        destination to router.determine(destination)
    }

    private fun deliver(
        outbox: Outbox,
        deliverers: Result<Pair<DeliveryDestination, List<OutboxDeliverer<DeliveryDestination>>>>,
    ) = deliverers.mapCatching { (destination, selected) ->
        selected.forEach { deliverer -> deliverer.send(outbox, destination) }
    }

    private fun saveResult(outbox: Outbox, delivery: Result<Unit>) {
        try {
            delivery.fold(
                onSuccess = { service.complete(outbox) },
                onFailure = { failure -> service.fail(outbox, failure) },
            )
        } catch (_: OutboxOptimisticLockException) {
            // The lock expired and another instance took the record over
        }
    }
}
