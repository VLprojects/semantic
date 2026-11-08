package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxStatus
import java.time.Instant

class OutboxTests {

    private val moment = Instant.parse("2026-11-09T00:00:00Z")

    private fun createOutbox() = Outbox(
        id = "record",
        key = "order-1",
        delivererId = "deliverer",
        payloadType = "OrderPlaced",
        payload = "{}",
        idempotencyKey = "operation",
        createdAt = moment,
        nextRetryAt = moment,
        context = mapOf("trace" to "abc"),
    )

    @Test
    fun `a new record has no failures and no lease`() {
        val record = createOutbox()

        assertEquals(OutboxStatus.NEW, record.status)
        assertEquals(0, record.failureCount)
        assertNull(record.leaseToken)
        assertNull(record.leaseExpiresAt)
        assertNull(record.completedAt)
        assertNull(record.lastError)
    }

    @Test
    fun `the metadata repeats what a record says about itself`() {
        val metadata = createOutbox().metadata

        assertEquals("record", metadata.id)
        assertEquals("order-1", metadata.key)
        assertEquals("deliverer", metadata.delivererId)
        assertEquals("OrderPlaced", metadata.payloadType)
        assertEquals("operation", metadata.idempotencyKey)
        assertEquals(0, metadata.failureCount)
        assertEquals(moment, metadata.createdAt)
        assertEquals(mapOf("trace" to "abc"), metadata.context)
    }
}
