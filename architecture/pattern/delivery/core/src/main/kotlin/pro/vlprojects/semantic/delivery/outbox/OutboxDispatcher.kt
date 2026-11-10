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
        ?.let { failed -> log(failed, error) }

    private fun skipOnOptimisticLock(outbox: Outbox, save: () -> Outbox) = try {
        save()
    } catch (_: OutboxOptimisticLockException) {
        logger
            .atWarn()
            .log { "Outbox ${outbox.id} was changed by another process during delivery" }
        null
    }

    private fun log(outbox: Outbox, error: Throwable) {
        val delivery = outbox.delivery

        logger
            .atLevel(resolveLevel(delivery.status))
            .setCause(error)
            .log { "Outbox ${outbox.id} failed ${delivery.attempts} times and is ${delivery.status}" }
    }

    private fun resolveLevel(status: DeliveryStatus) = when (status) {
        DeliveryStatus.CANCELED -> config.logging.cancellation.level
        else -> config.logging.failure.level
    }
}
