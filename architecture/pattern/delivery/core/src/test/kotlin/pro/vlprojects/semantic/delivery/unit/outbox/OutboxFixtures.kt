package pro.vlprojects.semantic.delivery.unit.outbox

import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxId
import pro.vlprojects.semantic.delivery.outbox.OutboxPayload
import java.time.Instant

internal fun createOutbox(createdAt: Instant, id: String = "record") = Outbox.create(
    id = OutboxId(id),
    orderingKey = "order-1",
    idempotencyKey = "operation",
    delivererName = "deliverer",
    payload = OutboxPayload("OrderPlaced", byteArrayOf(1)),
    context = mapOf("trace" to "abc"),
    createdAt = createdAt,
)
