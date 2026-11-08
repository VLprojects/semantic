package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxId
import pro.vlprojects.semantic.delivery.outbox.OutboxRepository
import pro.vlprojects.semantic.delivery.outbox.OutboxService
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class OutboxServiceTests {

    private val moment = Instant.parse("2026-11-09T00:00:00Z")
    private val clock = Clock.fixed(moment, ZoneOffset.UTC)
    private val lockDuration = Duration.ofMinutes(1)
    private val cause = IllegalStateException("timeout")
    private val lockedOutboxes = mutableListOf<Outbox>()
    private val lockRequests = mutableListOf<Pair<Instant, Instant>>()

    private val repository = object : OutboxRepository {

        override fun save(outbox: Outbox) = outbox

        override fun findById(id: OutboxId) = lockedOutboxes.find { it.id == id }

        override fun lockScheduledForDelivery(
            scheduledUntil: Instant,
            lockedUntil: Instant,
            limit: Int,
        ) = lockedOutboxes
            .take(limit)
            .also { lockRequests += scheduledUntil to lockedUntil }

        override fun deleteAll(status: DeliveryStatus, terminatedBefore: Instant, limit: Int) = 0
    }

    private val service = OutboxService(repository, lockDuration, clock)
    private val started = createStartedOutbox(moment, moment.plus(lockDuration))

    @Test
    fun `getting with a lock asks the repository to hold scheduled records`() {
        lockedOutboxes += started

        val locked = service.getAllWithLock(10)

        assertEquals(listOf(started), locked)
        assertEquals(listOf(moment to moment.plus(lockDuration)), lockRequests)
    }

    @Test
    fun `rescheduling plans the next attempt after the delay`() {
        val rescheduled = service.reschedule(started, cause, Duration.ofSeconds(30))

        assertEquals(DeliveryStatus.FAILED, rescheduled.delivery.status)
        assertEquals(moment.plusSeconds(30), rescheduled.delivery.scheduledAt)
    }

    @Test
    fun `completing records the current time`() {
        val completed = service.complete(started)

        assertEquals(DeliveryStatus.COMPLETED, completed.delivery.status)
        assertEquals(moment, completed.delivery.terminatedAt)
    }

    @Test
    fun `canceling stores the cause and terminates the delivery`() {
        val canceled = service.cancel(started, cause)

        assertEquals(DeliveryStatus.CANCELED, canceled.delivery.status)
        assertEquals("java.lang.IllegalStateException: timeout", canceled.delivery.error)
        assertEquals(moment, canceled.delivery.terminatedAt)
    }
}
