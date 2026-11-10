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
        .onEach(::deliver)
        .size

    private fun deliver(outbox: Outbox) {
        val failure = catchDeliveryFailure(outbox)

        try {
            when (failure) {
                null -> service.complete(outbox)
                else -> service.fail(outbox, failure)
            }
        } catch (_: OutboxOptimisticLockException) {
            // The lock expired and another instance took the record over
        }
    }

    private fun catchDeliveryFailure(outbox: Outbox) = try {
        send(outbox)
        null
    } catch (exception: Exception) {
        exception
    }

    private fun send(outbox: Outbox) {
        val destination = deserializer.deserialize(outbox.destination)

        router
            .determine(destination)
            .forEach { it.send(outbox, destination) }
    }
}
