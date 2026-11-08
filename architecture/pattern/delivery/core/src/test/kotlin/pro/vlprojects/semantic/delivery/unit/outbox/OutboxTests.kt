package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.OutboxPayload
import java.time.Instant

class OutboxTests {

    private val moment = Instant.parse("2026-11-09T00:00:00Z")
    private val later = moment.plusSeconds(60)

    @Test
    fun `a new record awaits delivery from its creation`() {
        val delivery = createOutbox(moment).delivery

        assertEquals(DeliveryStatus.AWAITING, delivery.status)
        assertEquals(0, delivery.attempts)
        assertEquals(moment, delivery.scheduledAt)
        assertNull(delivery.lockedUntil)
    }

    @Test
    fun `starting a delivery counts the attempt and locks the record`() {
        val delivery = createOutbox(moment).startDelivery(later).delivery

        assertEquals(DeliveryStatus.IN_PROGRESS, delivery.status)
        assertEquals(1, delivery.attempts)
        assertEquals(later, delivery.lockedUntil)
    }

    @Test
    fun `a failed delivery is unlocked and scheduled again`() {
        val delivery = createOutbox(moment)
            .startDelivery(later)
            .failDelivery(later, "timeout")
            .delivery

        assertEquals(DeliveryStatus.FAILED, delivery.status)
        assertEquals(later, delivery.scheduledAt)
        assertEquals("timeout", delivery.failureReason)
        assertNull(delivery.lockedUntil)
    }

    @Test
    fun `a long failure reason is cut`() {
        val delivery = createOutbox(moment)
            .startDelivery(later)
            .abandonDelivery("x".repeat(2000))
            .delivery

        assertEquals(1000, delivery.failureReason?.length)
    }

    @Test
    fun `a completed delivery cannot start again`() {
        val outbox = createOutbox(moment)
            .startDelivery(later)
            .completeDelivery(later)

        assertThrows<IllegalStateException> { outbox.startDelivery(later) }
    }

    @Test
    fun `a delivery that is not in progress cannot complete`() {
        val outbox = createOutbox(moment)

        assertThrows<IllegalStateException> { outbox.completeDelivery(later) }
    }

    @Test
    fun `payloads with equal bytes are equal`() {
        val first = OutboxPayload("OrderPlaced", byteArrayOf(1, 2))
        val second = OutboxPayload("OrderPlaced", byteArrayOf(1, 2))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }
}
