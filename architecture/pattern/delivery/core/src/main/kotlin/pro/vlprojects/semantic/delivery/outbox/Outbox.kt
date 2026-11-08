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
    delivery: Delivery,
    version: Long,
) {

    public var delivery: Delivery = delivery
        private set

    public var version: Long = version
        private set

    public fun updateVersion(version: Long) {
        this.version = version
    }

    internal fun start(lockedUntil: Instant) {
        delivery = delivery.started(lockedUntil)
    }

    internal fun complete(terminatedAt: Instant) {
        delivery = delivery.completed(terminatedAt)
    }

    internal fun fail(scheduledAt: Instant, error: String) {
        delivery = delivery.failed(scheduledAt, error)
    }

    internal fun cancel(terminatedAt: Instant) {
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
            createdAt: Instant,
            delivery: Delivery = Delivery.of(scheduledAt = createdAt),
            version: Long = 0,
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
