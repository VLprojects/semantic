package pro.vlprojects.semantic.delivery.outbox

import org.slf4j.LoggerFactory
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.error.OutboxConflictException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryTimeoutException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException
import pro.vlprojects.semantic.delivery.outbox.id.OutboxIdGenerator
import java.time.Clock
import java.time.Duration

public class OutboxService(
    private val repository: OutboxRepository,
    private val serializer: PayloadSerializer,
    private val contextCollector: ContextCollector,
    private val destinationSerializer: DeliveryDestinationSerializer,
    private val idGenerator: OutboxIdGenerator,
    private val config: OutboxProperties,
    private val clock: Clock,
) {

    private val logger = LoggerFactory.getLogger(OutboxService::class.java)

    @Throws(OutboxConflictException::class)
    public fun create(payload: Any, destination: DeliveryDestination): Outbox {
        val body = serializer.serialize(payload)

        return Outbox
            .of(
                id = idGenerator.generate(),
                payload = Payload.of(payload::class, body),
                destination = destinationSerializer.serialize(destination),
                context = contextCollector.collect(payload),
                clock = clock,
            )
            .let(repository::save)
    }

    public fun acquireScheduledForDelivery(limit: Int): List<Outbox> {
        val startedAt = clock.instant()

        return repository.findAndModifyScheduled(startedAt, limit)
    }

    public fun recoverStale(limit: Int): List<Outbox> {
        val staleness = config.staleness.processing
        val startedBefore = clock.instant().minus(staleness)

        return repository
            .findAllProcessing(startedBefore, limit)
            .mapNotNull { outbox -> recover(outbox, staleness) }
    }

    public fun complete(outbox: Outbox): Outbox = outbox
        .complete(clock.instant())
        .let(repository::save)

    public fun fail(outbox: Outbox, cause: Throwable): Outbox {
        val error = config.errorPolicy.extract(cause)

        return when {
            isRetryable(outbox) -> reschedule(outbox, error)
            else -> cancel(outbox, error)
        }
    }

    private fun isRetryable(outbox: Outbox) = outbox.delivery.attempts < config.maxAttempts

    private fun reschedule(outbox: Outbox, error: String): Outbox {
        val delay = config.backoff.calculate(outbox.delivery.attempts)
        val scheduledAt = clock.instant().plus(delay)

        return outbox
            .fail(scheduledAt, error)
            .let(repository::save)
    }

    private fun cancel(outbox: Outbox, error: String): Outbox {
        val now = clock.instant()

        return outbox
            .fail(now, error)
            .cancel(now)
            .let(repository::save)
    }

    private fun recover(outbox: Outbox, staleness: Duration): Outbox? {
        val cause = OutboxDeliveryTimeoutException(
            "Delivery started at ${outbox.delivery.startedAt} has not finished within $staleness",
        )

        return try {
            fail(outbox, cause).also { recovered -> logRecovery(recovered, staleness) }
        } catch (_: OutboxOptimisticLockException) {
            logger
                .atDebug()
                .log { "Outbox ${outbox.id} was changed by another process before recovery" }
            null
        }
    }

    private fun logRecovery(outbox: Outbox, staleness: Duration) {
        val delivery = outbox.delivery

        delivery.status
            .let(::resolveLevel)
            .let(logger::atLevel)
            .log { "Outbox ${outbox.id} was processing longer than $staleness and is ${delivery.status}" }
    }

    private fun resolveLevel(status: DeliveryStatus) = when (status) {
        DeliveryStatus.CANCELED -> config.logging.cancellation.level
        else -> config.logging.failure.level
    }
}
