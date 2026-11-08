package pro.vlprojects.semantic.delivery.unit.outbox

import pro.vlprojects.semantic.delivery.outbox.Delivery
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxId
import pro.vlprojects.semantic.delivery.outbox.Payload
import java.time.Instant

internal fun createOutbox(
    createdAt: Instant,
    delivery: Delivery = Delivery.of(createdAt),
) = Outbox.of(
    id = OutboxId.of("record"),
    orderingKey = "order-1",
    idempotencyKey = "operation",
    deliverer = "deliverer",
    payload = Payload.of("OrderPlaced", byteArrayOf(1)),
    context = mapOf("trace" to "abc"),
    createdAt = createdAt,
    delivery = delivery,
)

internal fun createStartedOutbox(createdAt: Instant, lockedUntil: Instant) = Delivery
    .restore(
        scheduledAt = createdAt,
        status = DeliveryStatus.STARTED,
        attempts = 1,
        lockedUntil = lockedUntil,
        terminatedAt = null,
        error = null,
    )
    .let { createOutbox(createdAt, it) }
