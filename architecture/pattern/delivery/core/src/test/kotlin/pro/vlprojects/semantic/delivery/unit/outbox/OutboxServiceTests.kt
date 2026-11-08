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
    private val readyOutboxes = listOf(createOutbox(moment))

    private fun createRepository(ready: List<Outbox>, conflictingId: String? = null) = object : OutboxRepository {

        override fun save(outbox: Outbox) = when (outbox.id.value) {
            conflictingId -> throw OutboxVersionConflictException(outbox.id)
            else -> outbox
        }

        override fun findById(id: OutboxId) = ready.find { it.id == id }

        override fun findReadyForDelivery(now: Instant, limit: Int) = ready.take(limit)

        override fun deleteAll(status: DeliveryStatus, createdBefore: Instant, limit: Int) = 0
    }

    private fun createService(ready: List<Outbox>, conflictingId: String? = null) = createRepository(
        ready,
        conflictingId,
    )
        .let { OutboxService(it, lockDuration, clock) }

    @Test
    fun `locking holds ready records for the lock duration`() {
        val locked = createService(ready = readyOutboxes).lockForDelivery(10).single()

        assertEquals(DeliveryStatus.IN_PROGRESS, locked.delivery.status)
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
        val service = createService(ready = readyOutboxes)
        val locked = service.lockForDelivery(1).single()

        val retried = service.retryDelivery(locked, "timeout", Duration.ofSeconds(30))

        assertEquals(DeliveryStatus.FAILED, retried.delivery.status)
        assertEquals(moment.plusSeconds(30), retried.delivery.scheduledAt)
    }

    @Test
    fun `completing records the current time`() {
        val service = createService(ready = readyOutboxes)

        val completed = service
            .lockForDelivery(1)
            .single()
            .let(service::completeDelivery)

        assertEquals(DeliveryStatus.COMPLETED, completed.delivery.status)
        assertEquals(moment, completed.delivery.completedAt)
    }

    @Test
    fun `canceling keeps the reason`() {
        val service = createService(ready = readyOutboxes)

        val canceled = service
            .lockForDelivery(1)
            .single()
            .let { service.cancelDelivery(it, "exhausted") }

        assertEquals(DeliveryStatus.CANCELED, canceled.delivery.status)
        assertEquals("exhausted", canceled.delivery.error)
    }
}
