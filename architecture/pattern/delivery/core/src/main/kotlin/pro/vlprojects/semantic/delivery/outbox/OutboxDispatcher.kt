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
        .onFailure { error -> fail(outbox, error) }

    private fun deliver(outbox: Outbox) {
        val destination = deserializer.deserialize(outbox.destination)
        val deliverer = router.resolve(destination)

        deliverer.send(outbox, destination)
    }

    private fun complete(outbox: Outbox) = skipOnOptimisticLock { service.complete(outbox) }

    private fun fail(outbox: Outbox, cause: Throwable) = skipOnOptimisticLock { service.fail(outbox, cause) }

    private fun skipOnOptimisticLock(save: () -> Outbox) = try {
        save()
    } catch (_: OutboxOptimisticLockException) {
        // The lock expired and another instance took the record over
        null
    }
}
