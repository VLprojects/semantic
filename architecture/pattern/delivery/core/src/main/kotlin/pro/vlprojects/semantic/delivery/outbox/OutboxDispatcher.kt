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

    private fun process(outbox: Outbox) = runCatching { deliver(outbox) }
        .onSuccess { complete(outbox) }
        .onFailure { failure -> fail(outbox, failure) }

    private fun deliver(outbox: Outbox) {
        val destination = deserializer.deserialize(outbox.destination)
        val deliverer = router.resolve(destination)

        deliverer.send(outbox, destination)
    }

    private fun complete(outbox: Outbox) = ignoreTakeover { service.complete(outbox) }

    private fun fail(outbox: Outbox, cause: Throwable) = ignoreTakeover { service.fail(outbox, cause) }

    private fun ignoreTakeover(save: () -> Outbox) {
        try {
            save()
        } catch (_: OutboxOptimisticLockException) {
            // The lock expired and another instance took the record over
        }
    }
}
