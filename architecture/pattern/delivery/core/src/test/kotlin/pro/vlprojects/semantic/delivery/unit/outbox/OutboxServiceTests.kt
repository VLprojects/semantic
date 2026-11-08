package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxId
import pro.vlprojects.semantic.delivery.outbox.OutboxRepository
import pro.vlprojects.semantic.delivery.outbox.OutboxService
import pro.vlprojects.semantic.delivery.outbox.OutboxVersionConflictException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class OutboxServiceTests {

    private val moment = Instant.parse("2026-11-09T00:00:00Z")
    private val clock = Clock.fixed(moment, ZoneOffset.UTC)
    private val lockDuration = Duration.ofMinutes(1)
    private val cause = IllegalStateException("timeout")
    private val readyOutboxes = listOf(createOutbox(moment))

    private fun createRepository(ready: List<Outbox>, conflictingId: String? = null) = object : OutboxRepository {

        override fun save(outbox: Outbox) = when (outbox.id.value) {
            conflictingId -> throw OutboxVersionConflictException(outbox.id)
            else -> outbox
        }

        override fun findById(id: OutboxId) = ready.find { it.id == id }

        override fun findReadyForDelivery(until: Instant, limit: Int) = ready.take(limit)

        override fun deleteAll(status: DeliveryStatus, terminatedBefore: Instant, limit: Int) = 0
    }

    private fun createService(ready: List<Outbox>, conflictingId: String? = null) = createRepository(
        ready,
        conflictingId,
    )
        .let { OutboxService(it, lockDuration, clock) }

    @Test
    fun `locking holds ready records for the lock duration`() {
        val locked = createService(readyOutboxes).lockForDelivery(10).single()

        assertEquals(DeliveryStatus.STARTED, locked.delivery.status)
        assertEquals(moment.plus(lockDuration), locked.delivery.lockedUntil)
    }

    @Test
    fun `locking skips a record taken by another worker`() {
        val ready = listOf(createOutbox(moment, "taken"), createOutbox(moment, "free"))

        val locked = createService(ready, conflictingId = "taken").lockForDelivery(10)

        assertEquals(listOf(OutboxId("free")), locked.map(Outbox::id))
    }

    @Test
    fun `retrying plans the next attempt after the delay`() {
        val service = createService(readyOutboxes)
        val locked = service.lockForDelivery(1).single()

        val retried = service.retryDelivery(locked, cause, Duration.ofSeconds(30))

        assertEquals(DeliveryStatus.FAILED, retried.delivery.status)
        assertEquals(moment.plusSeconds(30), retried.delivery.scheduledAt)
    }

    @Test
    fun `completing records the current time`() {
        val service = createService(readyOutboxes)

        val completed = service
            .lockForDelivery(1)
            .single()
            .let(service::completeDelivery)

        assertEquals(DeliveryStatus.COMPLETED, completed.delivery.status)
        assertEquals(moment, completed.delivery.terminatedAt)
    }

    @Test
    fun `canceling stores the cause and terminates the delivery`() {
        val service = createService(readyOutboxes)

        val canceled = service
            .lockForDelivery(1)
            .single()
            .let { service.cancelDelivery(it, cause) }

        assertEquals(DeliveryStatus.CANCELED, canceled.delivery.status)
        assertEquals("java.lang.IllegalStateException: timeout", canceled.delivery.error)
        assertEquals(moment, canceled.delivery.terminatedAt)
    }
}
