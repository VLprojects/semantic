package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.context.Context
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.error.OutboxConflictException
import pro.vlprojects.semantic.delivery.outbox.id.OutboxIdGenerator
import java.time.Clock

public class OutboxService(
    private val repository: OutboxRepository,
    private val serializer: PayloadSerializer,
    private val contextCollector: ContextCollector,
    private val idGenerator: OutboxIdGenerator,
    private val config: OutboxProperties,
    private val clock: Clock,
) {

    @Throws(OutboxConflictException::class)
    public fun create(payload: Any, builderAction: Context.Builder.() -> Unit = {}): Outbox {
        val body = serializer.serialize(payload)

        return Outbox
            .of(
                id = idGenerator.generate(),
                payload = Payload.of(payload::class, body),
                context = contextCollector.collect(payload, builderAction),
                clock = clock,
            )
            .let(repository::save)
    }

    public fun getAllWithLock(limit: Int): List<Outbox> {
        val now = clock.instant()
        val lockedUntil = now.plus(config.lockDuration)

        return repository.lockScheduledForDelivery(now, lockedUntil, limit)
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
}
