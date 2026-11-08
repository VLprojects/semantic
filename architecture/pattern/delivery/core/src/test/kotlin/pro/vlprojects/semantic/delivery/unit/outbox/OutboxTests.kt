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
    private val outbox = createOutbox(moment)

    @Test
    fun `a new record is pending from its creation`() {
        val delivery = outbox.delivery

        assertEquals(DeliveryStatus.PENDING, delivery.status)
        assertEquals(0, delivery.attempts)
        assertEquals(moment, delivery.scheduledAt)
        assertNull(delivery.lockedUntil)
    }

    @Test
    fun `starting counts the attempt and locks the record`() {
        outbox.start(later)

        assertEquals(DeliveryStatus.STARTED, outbox.delivery.status)
        assertEquals(1, outbox.delivery.attempts)
        assertEquals(later, outbox.delivery.lockedUntil)
    }

    @Test
    fun `failing unlocks the record and schedules it again`() {
        outbox.start(later)
        outbox.fail(later, IllegalStateException("timeout"))

        assertEquals(DeliveryStatus.FAILED, outbox.delivery.status)
        assertEquals(later, outbox.delivery.scheduledAt)
        assertEquals("java.lang.IllegalStateException: timeout", outbox.delivery.error)
        assertNull(outbox.delivery.lockedUntil)
    }

    @Test
    fun `a long error is cut`() {
        outbox.start(later)
        outbox.fail(later, IllegalStateException("x".repeat(2000)))

        assertEquals(1000, outbox.delivery.error?.length)
    }

    @Test
    fun `completing records the termination time`() {
        outbox.start(later)
        outbox.complete(later)

        assertEquals(DeliveryStatus.COMPLETED, outbox.delivery.status)
        assertEquals(later, outbox.delivery.terminatedAt)
    }

    @Test
    fun `canceling keeps the last error and records the termination time`() {
        outbox.start(later)
        outbox.fail(later, IllegalStateException("timeout"))
        outbox.cancel(later)

        assertEquals(DeliveryStatus.CANCELED, outbox.delivery.status)
        assertEquals("java.lang.IllegalStateException: timeout", outbox.delivery.error)
        assertEquals(later, outbox.delivery.terminatedAt)
    }

    @Test
    fun `a delivery that has not failed cannot be canceled`() {
        outbox.start(later)

        assertThrows<IllegalStateException> { outbox.cancel(later) }
    }

    @Test
    fun `a completed delivery cannot start again`() {
        outbox.start(later)
        outbox.complete(later)

        assertThrows<IllegalStateException> { outbox.start(later) }
    }

    @Test
    fun `a delivery that has not started cannot complete`() {
        assertThrows<IllegalStateException> { outbox.complete(later) }
    }

    @Test
    fun `payloads with equal bytes are equal`() {
        val first = Payload.of("OrderPlaced", byteArrayOf(1, 2))
        val second = Payload.of("OrderPlaced", byteArrayOf(1, 2))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `a typed payload takes its type from the class`() {
        assertEquals("java.lang.String", Payload.of<String>(byteArrayOf(1)).type)
    }
}
