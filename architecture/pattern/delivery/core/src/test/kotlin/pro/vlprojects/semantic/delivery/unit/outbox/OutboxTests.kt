package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Payload
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
        val delivery = createOutbox(moment).markInProgress(later).delivery

        assertEquals(DeliveryStatus.IN_PROGRESS, delivery.status)
        assertEquals(1, delivery.attempts)
        assertEquals(later, delivery.lockedUntil)
    }

    @Test
    fun `a failed delivery is unlocked and scheduled again`() {
        val delivery = createOutbox(moment)
            .markInProgress(later)
            .markFailed(later, "timeout")
            .delivery

        assertEquals(DeliveryStatus.FAILED, delivery.status)
        assertEquals(later, delivery.scheduledAt)
        assertEquals("timeout", delivery.error)
        assertNull(delivery.lockedUntil)
    }

    @Test
    fun `a long error is cut`() {
        val delivery = createOutbox(moment)
            .markInProgress(later)
            .markAbandoned("x".repeat(2000))
            .delivery

        assertEquals(1000, delivery.error?.length)
    }

    @Test
    fun `a completed delivery cannot start again`() {
        val outbox = createOutbox(moment)
            .markInProgress(later)
            .markCompleted(later)

        assertThrows<IllegalStateException> { outbox.markInProgress(later) }
    }

    @Test
    fun `a delivery that is not in progress cannot complete`() {
        val outbox = createOutbox(moment)

        assertThrows<IllegalStateException> { outbox.markCompleted(later) }
    }

    @Test
    fun `payloads with equal bytes are equal`() {
        val first = Payload.restore("OrderPlaced", byteArrayOf(1, 2))
        val second = Payload.restore("OrderPlaced", byteArrayOf(1, 2))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `a typed payload takes its type from the class`() {
        assertEquals("java.lang.String", Payload.of<String>(byteArrayOf(1)).type)
    }
}
