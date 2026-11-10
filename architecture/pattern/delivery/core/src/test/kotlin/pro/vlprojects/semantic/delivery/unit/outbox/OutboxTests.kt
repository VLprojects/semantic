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
    private val processing = createProcessingOutbox(moment, later)

    @Test
    fun `a new record is pending from its creation`() {
        val delivery = createOutbox(moment).delivery

        assertEquals(DeliveryStatus.PENDING, delivery.status)
        assertEquals(0, delivery.attempts)
        assertEquals(moment, delivery.scheduledAt)
        assertNull(delivery.startedAt)
    }

    @Test
    fun `failing schedules the record again`() {
        val delivery = processing.fail(later, "timeout").delivery

        assertEquals(DeliveryStatus.FAILED, delivery.status)
        assertEquals(later, delivery.scheduledAt)
        assertEquals("timeout", delivery.error)
    }

    @Test
    fun `a long error is cut`() {
        val delivery = processing.fail(later, "x".repeat(2000)).delivery

        assertEquals(1000, delivery.error?.length)
    }

    @Test
    fun `completing records the termination time`() {
        val delivery = processing.complete(later).delivery

        assertEquals(DeliveryStatus.COMPLETED, delivery.status)
        assertEquals(later, delivery.terminatedAt)
    }

    @Test
    fun `canceling keeps the last error and records the termination time`() {
        val delivery = processing
            .fail(later, "timeout")
            .cancel(later)
            .delivery

        assertEquals(DeliveryStatus.CANCELED, delivery.status)
        assertEquals("timeout", delivery.error)
        assertEquals(later, delivery.terminatedAt)
    }

    @Test
    fun `a delivery that has not failed cannot be canceled`() {
        assertThrows<IllegalStateException> { processing.cancel(later) }
    }

    @Test
    fun `a delivery that is not processing cannot complete`() {
        val pending = createOutbox(moment)

        assertThrows<IllegalStateException> { pending.complete(later) }
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
