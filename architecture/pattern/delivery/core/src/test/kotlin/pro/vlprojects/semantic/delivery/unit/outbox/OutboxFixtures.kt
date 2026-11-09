package pro.vlprojects.semantic.delivery.unit.outbox

import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.Payload
import pro.vlprojects.semantic.delivery.outbox.context.Context
import pro.vlprojects.semantic.delivery.outbox.delivery.Delivery
import pro.vlprojects.semantic.delivery.outbox.delivery.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.id.OutboxId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private val identifier = OutboxId.of("record")
private val payload = Payload.of("OrderPlaced", byteArrayOf(1))
private val context = Context.of(
    target = "deliverer",
    orderingKey = "order-1",
    idempotencyKey = "operation",
    attributes = mapOf("trace" to "abc"),
)

internal fun createOutbox(createdAt: Instant) = Outbox.of(
    id = identifier,
    payload = payload,
    context = context,
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
            payload = payload,
            context = context,
            createdAt = createdAt,
            delivery = it,
            version = 1,
        )
    }
