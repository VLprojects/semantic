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
        try {
            deliver(outbox)
        } catch (exception: Exception) {
            return fail(outbox, exception)
        }

        complete(outbox)
    }

    private fun deliver(outbox: Outbox) {
        val destination = deserializer.deserialize(outbox.destination)
        val deliverers = router.determine(destination)

        deliverers.forEach { deliverer -> deliverer.send(outbox, destination) }
    }

    private fun complete(outbox: Outbox) = ignoreTakeover { service.complete(outbox) }

    private fun fail(outbox: Outbox, cause: Exception) = ignoreTakeover { service.fail(outbox, cause) }

    private fun ignoreTakeover(save: () -> Outbox) {
        try {
            save()
        } catch (_: OutboxOptimisticLockException) {
            // The lock expired and another instance took the record over
        }
    }
}
