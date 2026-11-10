package pro.vlprojects.semantic.delivery.outbox

import org.slf4j.LoggerFactory
import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException

public class OutboxDispatcher(
    private val service: OutboxService,
    private val deserializer: DeliveryDestinationDeserializer,
    private val router: OutboxDeliveryRouter,
    private val config: OutboxProperties,
) {

    private val logger = LoggerFactory.getLogger(OutboxDispatcher::class.java)

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

    private fun complete(outbox: Outbox) = skipOnOptimisticLock(outbox) { service.complete(outbox) }

    private fun fail(outbox: Outbox, error: Throwable) = skipOnOptimisticLock(outbox) { service.fail(outbox, error) }
        ?.let { failed -> logFailure(failed, error) }

    private fun skipOnOptimisticLock(outbox: Outbox, save: () -> Outbox) = try {
        save()
    } catch (_: OutboxOptimisticLockException) {
        logger.warn("Outbox {} was taken over by another instance after its lock expired", outbox.id)
        null
    }

    private fun logFailure(outbox: Outbox, error: Throwable) {
        val delivery = outbox.delivery

        logger
            .atLevel(selectLogLevel(delivery.status))
            .setCause(error)
            .log("Outbox {} failed {} times and is {}", outbox.id, delivery.attempts, delivery.status)
    }

    private fun selectLogLevel(status: DeliveryStatus) = when (status) {
        DeliveryStatus.CANCELED -> config.logging.cancellation.level
        else -> config.logging.failure.level
    }
}
