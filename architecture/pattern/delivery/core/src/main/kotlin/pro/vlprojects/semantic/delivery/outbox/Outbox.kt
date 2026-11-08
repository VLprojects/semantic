package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public class Outbox(
    public val id: OutboxId,
    public val orderingKey: String,
    public val idempotencyKey: String,
    public val deliverer: String,
    public val payload: Payload,
    public val context: Map<String, String>,
    public val createdAt: Instant,
    public val delivery: Delivery = Delivery(scheduledAt = createdAt),
    public val version: Long = 0,
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
}
