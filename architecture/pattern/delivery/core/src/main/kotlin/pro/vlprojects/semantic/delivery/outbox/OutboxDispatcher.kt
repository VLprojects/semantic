package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException

public class OutboxDispatcher(
    private val service: OutboxService,
    private val router: OutboxDeliveryRouter,
    private val config: OutboxProperties,
) {

    public fun dispatch(): Int = service
        .getScheduledWithLock(config.batchSize)
        .onEach(::deliver)
        .size

    private fun deliver(outbox: Outbox) {
        val failure = send(outbox)

        try {
            when (failure) {
                null -> service.complete(outbox)
                else -> service.fail(outbox, failure)
            }
        } catch (_: OutboxOptimisticLockException) {
            // The lock expired and another instance took the record over
        }
    }

    private fun send(outbox: Outbox) = try {
        router.send(outbox)
        null
    } catch (exception: Exception) {
        exception
    }
}
