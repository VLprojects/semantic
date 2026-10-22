package pro.vlprojects.semantic.ddd.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.identity.Id
import java.time.Instant
import java.util.UUID

class BaseDomainEventTest {
    @Test
    fun `events with the same id are equal`() {
        val eventId = UUID.randomUUID()

        assertEquals(Opened(OrderId(1), eventId), Opened(OrderId(2), eventId))
        assertEquals(Opened(OrderId(1), eventId).hashCode(), Opened(OrderId(2), eventId).hashCode())
    }

    @Test
    fun `events get ids of their own`() {
        assertNotEquals(Opened(OrderId(1)), Opened(OrderId(1)))
    }

    @Test
    fun `an event keeps the id and the time passed in`() {
        val eventId = UUID.randomUUID()
        val occurredAt = Instant.parse("2026-10-23T12:00:00Z")

        val event = Opened(OrderId(1), eventId, occurredAt)

        assertEquals(eventId, event.eventId)
        assertEquals(occurredAt, event.occurredAt)
    }

    @JvmInline
    private value class OrderId(override val value: Int) : Id<Int>

    private class Opened(
        override val aggregate: OrderId,
        eventId: UUID = UUID.randomUUID(),
        occurredAt: Instant = Instant.now(),
    ) : BaseDomainEvent<OrderId>(eventId, occurredAt) {
        override val eventType: String get() = "order.opened"
    }
}
