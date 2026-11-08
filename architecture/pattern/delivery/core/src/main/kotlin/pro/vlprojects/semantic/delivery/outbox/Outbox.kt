package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public class Outbox private constructor(
    public val id: OutboxId,
    public val orderingKey: String,
    public val idempotencyKey: String,
    public val delivererName: String,
    public val payload: OutboxPayload,
    public val context: Map<String, String>,
    public val createdAt: Instant,
    public val delivery: OutboxDelivery,
    public val version: Long,
) {

    internal fun startDelivery(lockedUntil: Instant) = lockedUntil
        .let(delivery::start)
        .let(::replaceDelivery)

    internal fun completeDelivery(completedAt: Instant) = completedAt
        .let(delivery::complete)
        .let(::replaceDelivery)

    internal fun failDelivery(scheduledAt: Instant, reason: String) = delivery
        .fail(scheduledAt, reason)
        .let(::replaceDelivery)

    internal fun abandonDelivery(reason: String) = reason
        .let(delivery::abandon)
        .let(::replaceDelivery)

    private fun replaceDelivery(delivery: OutboxDelivery) = Outbox(
        id = id,
        orderingKey = orderingKey,
        idempotencyKey = idempotencyKey,
        delivererName = delivererName,
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
            delivererName: String,
            payload: OutboxPayload,
            context: Map<String, String>,
            createdAt: Instant,
        ): Outbox = Outbox(
            id = id,
            orderingKey = orderingKey,
            idempotencyKey = idempotencyKey,
            delivererName = delivererName,
            payload = payload,
            context = context,
            createdAt = createdAt,
            delivery = OutboxDelivery.schedule(createdAt),
            version = 0,
        )

        public fun restore(
            id: OutboxId,
            orderingKey: String,
            idempotencyKey: String,
            delivererName: String,
            payload: OutboxPayload,
            context: Map<String, String>,
            createdAt: Instant,
            delivery: OutboxDelivery,
            version: Long,
        ): Outbox = Outbox(
            id = id,
            orderingKey = orderingKey,
            idempotencyKey = idempotencyKey,
            delivererName = delivererName,
            payload = payload,
            context = context,
            createdAt = createdAt,
            delivery = delivery,
            version = version,
        )
    }
}
