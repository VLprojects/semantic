package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public class Outbox private constructor(
    public val id: OutboxId,
    public val orderingKey: String,
    public val idempotencyKey: String,
    public val deliverer: String,
    public val payload: Payload,
    public val context: Map<String, String>,
    public val createdAt: Instant,
    public val delivery: Delivery,
    public val version: Long,
) {

    internal fun markInProgress(lockedUntil: Instant) = lockedUntil
        .let(delivery::markInProgress)
        .let(::replaceDelivery)

    internal fun markCompleted(completedAt: Instant) = completedAt
        .let(delivery::markCompleted)
        .let(::replaceDelivery)

    internal fun markFailed(scheduledAt: Instant, error: String) = delivery
        .markFailed(scheduledAt, error)
        .let(::replaceDelivery)

    internal fun markAbandoned(error: String) = error
        .let(delivery::markAbandoned)
        .let(::replaceDelivery)

    private fun replaceDelivery(delivery: Delivery) = Outbox(
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

    public companion object {

        public fun create(
            id: OutboxId,
            orderingKey: String,
            idempotencyKey: String,
            deliverer: String,
            payload: Payload,
            context: Map<String, String>,
            createdAt: Instant,
        ): Outbox = Outbox(
            id = id,
            orderingKey = orderingKey,
            idempotencyKey = idempotencyKey,
            deliverer = deliverer,
            payload = payload,
            context = context,
            createdAt = createdAt,
            delivery = Delivery(scheduledAt = createdAt),
            version = 0,
        )

        public fun restore(
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
