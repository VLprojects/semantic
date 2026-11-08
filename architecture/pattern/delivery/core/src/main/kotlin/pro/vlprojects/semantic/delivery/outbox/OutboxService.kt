package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.annotation.readIdempotencyKey
import pro.vlprojects.semantic.delivery.outbox.context.OutboxContextProvider
import java.time.Clock
import java.util.UUID

public class OutboxService(
    private val serializer: OutboxPayloadSerializer,
    private val contextProviders: List<OutboxContextProvider> = emptyList(),
    private val clock: Clock = Clock.systemUTC(),
) {

    public fun create(
        payload: Any,
        key: String,
        delivererId: String,
        context: Map<String, String> = emptyMap(),
        idempotencyKey: String? = null,
    ): Outbox = assemble(generateId(), key, payload, delivererId, context, idempotencyKey)

    public fun create(payload: Any, delivererId: String): Outbox = generateId()
        .let { id -> assemble(id, id, payload, delivererId, emptyMap(), null) }

    private fun generateId() = UUID.randomUUID().toString()

    private fun assemble(
        id: String,
        key: String,
        payload: Any,
        delivererId: String,
        context: Map<String, String>,
        idempotencyKey: String?,
    ) = clock.instant().let { now ->
        Outbox(
            id = id,
            key = key,
            delivererId = delivererId,
            payloadType = payload.javaClass.name,
            payload = serializer.serialize(payload),
            idempotencyKey = idempotencyKey ?: payload.readIdempotencyKey() ?: id,
            createdAt = now,
            nextRetryAt = now,
            context = collectContext(context),
        )
    }

    private fun collectContext(explicit: Map<String, String>) = contextProviders
        .flatMap { provider -> provider.provide().toList() }
        .toMap()
        .plus(explicit)
}
