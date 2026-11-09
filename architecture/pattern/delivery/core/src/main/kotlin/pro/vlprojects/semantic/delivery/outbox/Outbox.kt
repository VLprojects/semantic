package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.context.Context
import pro.vlprojects.semantic.delivery.outbox.delivery.Delivery
import pro.vlprojects.semantic.delivery.outbox.id.OutboxId
import java.time.Clock
import java.time.Instant

public class Outbox private constructor(
    public val id: OutboxId,
    public val payload: Payload,
    public val context: Context,
    public val createdAt: Instant,
    delivery: Delivery,
    public val version: Long,
) {

    public var delivery: Delivery = delivery
        private set

    internal fun complete(completedAt: Instant) = apply {
        delivery = delivery.completed(completedAt)
    }

    internal fun fail(scheduledAt: Instant, error: String) = apply {
        delivery = delivery.failed(scheduledAt, error)
    }

    internal fun cancel(canceledAt: Instant) = apply {
        delivery = delivery.canceled(canceledAt)
    }

    public companion object Factory {

        public fun of(
            id: OutboxId,
            payload: Payload,
            context: Context,
            clock: Clock = Clock.systemUTC(),
        ): Outbox {
            val createdAt = clock.instant()
            val delivery = Delivery.of(createdAt)

            return Outbox(
                id = id,
                payload = payload,
                context = context,
                createdAt = createdAt,
                delivery = delivery,
                version = 0,
            )
        }

        public fun of(
            id: OutboxId,
            payload: Payload,
            context: Context,
            createdAt: Instant,
            delivery: Delivery,
            version: Long,
        ): Outbox = Outbox(
            id = id,
            payload = payload,
            context = context,
            createdAt = createdAt,
            delivery = delivery,
            version = version,
        )
    }
}
