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
        val failure = try {
            val destination = deserializer.deserialize(outbox.destination)
            val deliverers = router.determine(destination)

            deliver(outbox, destination, deliverers)
            null
        } catch (exception: Exception) {
            exception
        }

        saveResult(outbox, failure)
    }

    private fun deliver(
        outbox: Outbox,
        destination: DeliveryDestination,
        deliverers: List<OutboxDeliverer<DeliveryDestination>>,
    ) = deliverers.forEach { deliverer -> deliverer.send(outbox, destination) }

    private fun saveResult(outbox: Outbox, failure: Exception?) {
        try {
            when (failure) {
                null -> service.complete(outbox)
                else -> service.fail(outbox, failure)
            }
        } catch (_: OutboxOptimisticLockException) {
            // The lock expired and another instance took the record over
        }
    }
}
