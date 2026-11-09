package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException

public class OutboxDispatcher(
    private val service: OutboxService,
    private val router: OutboxDeliveryRouter,
    private val config: OutboxProperties,
) {

    public fun dispatch(): Int = service
        .getAllWithLock(config.batchSize)
        .onEach(::deliver)
        .size

    private fun deliver(outbox: Outbox) {
        val failure = route(outbox)

        try {
            when (failure) {
                null -> service.complete(outbox)
                else -> service.fail(outbox, failure)
            }
        } catch (_: OutboxOptimisticLockException) {
            // The lock expired and another instance took the record over
        }
    }

    private fun route(outbox: Outbox) = try {
        router.route(outbox)
        null
    } catch (exception: Exception) {
        exception
    }
}
