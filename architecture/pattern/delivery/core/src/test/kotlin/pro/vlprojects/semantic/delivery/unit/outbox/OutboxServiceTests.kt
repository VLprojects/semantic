package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxProperties
import pro.vlprojects.semantic.delivery.outbox.OutboxRepository
import pro.vlprojects.semantic.delivery.outbox.OutboxService
import pro.vlprojects.semantic.delivery.outbox.Payload
import pro.vlprojects.semantic.delivery.outbox.PayloadSerializer
import pro.vlprojects.semantic.delivery.outbox.context.AttributeProvider
import pro.vlprojects.semantic.delivery.outbox.context.Context
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.context.ContextProvider
import pro.vlprojects.semantic.delivery.outbox.id.OutboxId
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
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
    private val savedOutboxes = mutableListOf<Outbox>()
    private val generatedId = OutboxId.of("generated")
    private val context = Context.of("rabbit")
    private val attributeProvider = AttributeProvider { mapOf("tenant" to "collected", "trace" to "abc") }
    private val targetProvider = ContextProvider { _, context ->
        context.takeIf(Context::hasTarget) ?: context.withTarget("provided")
    }
    private val contextCollector = listOf(targetProvider, attributeProvider).let(::ContextCollector)
    private val serialized = byteArrayOf(1)

    private val serializer = PayloadSerializer { serialized }

    private val repository = object : OutboxRepository {

        override fun save(outbox: Outbox) = outbox.also(savedOutboxes::add)

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

    private val backoff = FixedBackoff.ofSeconds(30)
    private val service = createService(maxAttempts = 2)
    private val started = createStartedOutbox(moment, moment.plus(lockDuration))

    private fun createService(maxAttempts: Int) = OutboxProperties(
        lockDuration = lockDuration,
        maxAttempts = maxAttempts,
        backoff = backoff,
    ).let { OutboxService(repository, serializer, contextCollector, { generatedId }, it, clock) }

    @Test
    fun `creating saves a pending record scheduled for now`() {
        val created = service.create("order", context)

        assertEquals(listOf(created), savedOutboxes)
        assertEquals(generatedId, created.id)
        assertEquals("rabbit", created.context.target)
        assertEquals(Payload.of(String::class, serialized), created.payload)
        assertEquals(DeliveryStatus.PENDING, created.delivery.status)
        assertEquals(moment, created.delivery.scheduledAt)
    }

    @Test
    fun `creating without a context asks the provider`() {
        val created = service.create("order")

        assertEquals("provided", created.context.target)
    }

    @Test
    fun `creating prefers the given attributes to the collected ones`() {
        val attributed = Context.of("rabbit", attributes = mapOf("tenant" to "given"))

        val created = service.create("order", attributed)

        assertEquals(mapOf("tenant" to "given", "trace" to "abc"), created.context.attributes)
    }

    @Test
    fun `getting with a lock asks the repository to hold scheduled records`() {
        lockedOutboxes += started

        val locked = service.getAllWithLock(10)

        assertEquals(listOf(started), locked)
        assertEquals(listOf(moment to moment.plus(lockDuration)), lockRequests)
    }

    @Test
    fun `failing with attempts left plans the next attempt after the backoff`() {
        val failed = service.fail(started, cause)

        assertEquals(DeliveryStatus.FAILED, failed.delivery.status)
        assertEquals(moment.plusSeconds(30), failed.delivery.scheduledAt)
    }

    @Test
    fun `completing records the current time`() {
        val completed = service.complete(started)

        assertEquals(DeliveryStatus.COMPLETED, completed.delivery.status)
        assertEquals(moment, completed.delivery.terminatedAt)
    }

    @Test
    fun `failing without attempts left cancels the delivery and stores the cause`() {
        val canceled = createService(maxAttempts = 1).fail(started, cause)

        assertEquals(DeliveryStatus.CANCELED, canceled.delivery.status)
        assertEquals("java.lang.IllegalStateException: timeout", canceled.delivery.error)
        assertEquals(moment, canceled.delivery.terminatedAt)
    }
}
