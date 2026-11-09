package pro.vlprojects.semantic.delivery.unit.outbox

import pro.vlprojects.semantic.delivery.outbox.Delivery
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.Payload
import pro.vlprojects.semantic.delivery.outbox.id.OutboxId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private val identifier = OutboxId.of("record")
private val payload = Payload.of("OrderPlaced", byteArrayOf(1))

internal fun createOutbox(createdAt: Instant) = Outbox.of(
    id = identifier,
    orderingKey = "order-1",
    idempotencyKey = "operation",
    deliverer = "deliverer",
    payload = payload,
    attributes = mapOf("trace" to "abc"),
    clock = Clock.fixed(createdAt, ZoneOffset.UTC),
)

internal fun createStartedOutbox(createdAt: Instant, lockedUntil: Instant) = Delivery
    .of(
        scheduledAt = createdAt,
        status = DeliveryStatus.STARTED,
        attempts = 1,
        lockedUntil = lockedUntil,
        terminatedAt = null,
        error = null,
    )
    .let {
        Outbox.of(
            id = identifier,
            orderingKey = "order-1",
            idempotencyKey = "operation",
            deliverer = "deliverer",
            payload = payload,
            attributes = mapOf("trace" to "abc"),
            createdAt = createdAt,
            delivery = it,
            version = 1,
        )
    }
