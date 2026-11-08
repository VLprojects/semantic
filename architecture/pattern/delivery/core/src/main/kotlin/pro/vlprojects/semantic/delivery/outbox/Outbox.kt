package pro.vlprojects.semantic.delivery.outbox

import java.time.Clock
import java.time.Instant

public class Outbox private constructor(
    public val id: OutboxId,
    public val orderingKey: String,
    public val idempotencyKey: String,
    public val deliverer: String,
    public val payload: Payload,
    public val context: Map<String, String>,
    public val createdAt: Instant,
    delivery: Delivery,
    public val version: Long,
) {

    public var delivery: Delivery = delivery
        private set

    internal fun complete(terminatedAt: Instant) = apply {
        delivery = delivery.completed(terminatedAt)
    }

    internal fun fail(scheduledAt: Instant, error: String) = apply {
        delivery = delivery.failed(scheduledAt, error)
    }

    internal fun cancel(terminatedAt: Instant) = apply {
        delivery = delivery.canceled(terminatedAt)
    }

    public companion object Factory {

        public fun of(
            id: OutboxId,
            orderingKey: String,
            idempotencyKey: String,
            deliverer: String,
            payload: Payload,
            context: Map<String, String>,
            clock: Clock = Clock.systemUTC(),
        ): Outbox {
            val createdAt = clock.instant()
            val delivery = Delivery.of(createdAt)

            return Outbox(
                id = id,
                orderingKey = orderingKey,
                idempotencyKey = idempotencyKey,
                deliverer = deliverer,
                payload = payload,
                context = context,
                createdAt = createdAt,
                delivery = delivery,
                version = 0,
            )
        }

        public fun of(
            id: OutboxId,
            orderingKey: String,
            idempotencyKey: String,
            deliverer: String,
            payload: Payload,
            context: Map<String, String>,
            createdAt: Instant,
            delivery: Delivery,
            version: Long,
        ): Outbox = Outbox(
            id = id,
            orderingKey = orderingKey,
            idempotencyKey = idempotencyKey,
            deliverer = deliverer,
            payload = payload,
            context = context,
            createdAt = createdAt,
            delivery = delivery,
            version = version,
        )
    }
}
