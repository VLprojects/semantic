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
    private val dueOutbox = listOf(createOutbox(moment))

    private fun createRepository(due: List<Outbox>, conflictingId: String? = null) = object : OutboxRepository {

        override fun save(outbox: Outbox) = when (outbox.id.value) {
            conflictingId -> throw OutboxVersionConflictException(outbox.id)
            else -> outbox
        }

        override fun findById(id: OutboxId) = due.find { it.id == id }

        override fun findDue(now: Instant, limit: Int) = due.take(limit)

        override fun deleteCompletedBefore(instant: Instant, limit: Int) = 0
    }

    private fun createService(due: List<Outbox>, conflictingId: String? = null) = createRepository(due, conflictingId)
        .let { OutboxService(it, lockDuration, clock) }

    @Test
    fun `claiming locks due records for the lock duration`() {
        val claimed = createService(due = dueOutbox).claimDue(10).single()

        assertEquals(DeliveryStatus.IN_PROGRESS, claimed.delivery.status)
        assertEquals(moment.plus(lockDuration), claimed.delivery.lockedUntil)
    }

    @Test
    fun `claiming skips a record taken by another worker`() {
        val due = listOf(createOutbox(moment, "taken"), createOutbox(moment, "free"))

        val claimed = createService(due, conflictingId = "taken").claimDue(10)

        assertEquals(listOf(OutboxId("free")), claimed.map(Outbox::id))
    }

    @Test
    fun `rescheduling plans the next attempt after the delay`() {
        val service = createService(due = dueOutbox)
        val claimed = service.claimDue(1).single()

        val rescheduled = service.reschedule(claimed, "timeout", Duration.ofSeconds(30))

        assertEquals(DeliveryStatus.FAILED, rescheduled.delivery.status)
        assertEquals(moment.plusSeconds(30), rescheduled.delivery.scheduledAt)
    }

    @Test
    fun `completing records the current time`() {
        val service = createService(due = dueOutbox)

        val completed = service
            .claimDue(1)
            .single()
            .let(service::complete)

        assertEquals(DeliveryStatus.COMPLETED, completed.delivery.status)
        assertEquals(moment, completed.delivery.completedAt)
    }

    @Test
    fun `abandoning keeps the reason`() {
        val service = createService(due = dueOutbox)

        val abandoned = service
            .claimDue(1)
            .single()
            .let { service.abandon(it, "exhausted") }

        assertEquals(DeliveryStatus.ABANDONED, abandoned.delivery.status)
        assertEquals("exhausted", abandoned.delivery.failureReason)
    }
}
