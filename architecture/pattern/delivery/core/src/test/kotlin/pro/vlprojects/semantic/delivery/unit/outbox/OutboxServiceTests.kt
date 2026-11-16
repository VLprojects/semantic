package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestinationSerializer
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxProperties
import pro.vlprojects.semantic.delivery.outbox.OutboxRepository
import pro.vlprojects.semantic.delivery.outbox.OutboxService
import pro.vlprojects.semantic.delivery.outbox.Payload
import pro.vlprojects.semantic.delivery.outbox.PayloadSerializer
import pro.vlprojects.semantic.delivery.outbox.context.AttributeProvider
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.context.ContextProvider
import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException
import pro.vlprojects.semantic.delivery.outbox.id.OutboxId
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OutboxServiceTests {

    private val moment = Instant.parse("2026-11-09T00:00:00Z")
    private val clock = Clock.fixed(moment, ZoneOffset.UTC)
    private val cause = IllegalStateException("timeout")
    private val lockedOutboxes = mutableListOf<Outbox>()
    private val lockRequests = mutableListOf<Instant>()
    private val savedOutboxes = mutableListOf<Outbox>()
    private val staleOutboxes = mutableListOf<Outbox>()
    private val staleLookups = mutableListOf<Pair<Instant, Int>>()
    private val saveFailures = ArrayDeque<RuntimeException>()
    private val generatedId = OutboxId.of("generated")
    private val attributeProvider = AttributeProvider { mapOf("tenant" to "collected", "trace" to "abc") }
    private val keyProvider = ContextProvider { _, builder -> builder.setOrderingKey("provided") }
    private val contextCollector = listOf(keyProvider, attributeProvider).let(::ContextCollector)
    private val destination = object : DeliveryDestination {}
    private val serializedDestinations = mutableListOf<DeliveryDestination>()
    private val destinationSerializer = DeliveryDestinationSerializer { destination ->
        serializedDestinations += destination
        "orders"
    }
    private val serialized = byteArrayOf(1)

    private val serializer = PayloadSerializer { serialized }

    private val repository = object : OutboxRepository {

        override fun save(outbox: Outbox): Outbox {
            saveFailures.removeFirstOrNull()?.let { failure -> throw failure }
            savedOutboxes += outbox

            return outbox
        }

        override fun findById(id: OutboxId) = lockedOutboxes.find { outbox -> outbox.id == id }

        override fun findAndModifyScheduled(startedAt: Instant, limit: Int) = lockedOutboxes
            .take(limit)
            .also { lockRequests += startedAt }

        override fun findAllProcessing(startedBefore: Instant, limit: Int) = staleOutboxes
            .take(limit)
            .also { staleLookups += startedBefore to limit }

        override fun deleteAll(status: DeliveryStatus, terminatedBefore: Instant, limit: Int) = 0
    }

    private val backoff = FixedBackoff.ofSeconds(30)
    private val service = createService(maxAttempts = 2)
    private val processing = createProcessingOutbox(moment, moment)
    private val stale = createProcessingOutbox(moment.minusSeconds(3600), moment.minusSeconds(3600))

    private fun createService(maxAttempts: Int) = OutboxProperties(
        maxAttempts = maxAttempts,
        backoff = backoff,
    ).let { config ->
        OutboxService(
            repository,
            serializer,
            contextCollector,
            destinationSerializer,
            { generatedId },
            config,
            clock,
        )
    }

    @Test
    fun `creating saves a pending record scheduled for now`() {
        val created = service.create("order", destination)

        assertEquals(listOf(created), savedOutboxes)
        assertEquals(generatedId, created.id)
        assertEquals("orders", created.destination)
        assertEquals(listOf(destination), serializedDestinations)
        assertEquals("provided", created.context.orderingKey)
        assertEquals(mapOf("tenant" to "collected", "trace" to "abc"), created.context.attributes)
        assertEquals(Payload.of(String::class, serialized), created.payload)
        assertEquals(DeliveryStatus.PENDING, created.delivery.status)
        assertEquals(moment, created.delivery.scheduledAt)
    }

    @Test
    fun `acquiring takes scheduled records from the repository`() {
        lockedOutboxes += processing

        val acquiredOutboxes = service.acquireScheduledForDelivery(10)

        assertEquals(listOf(processing), acquiredOutboxes)
        assertEquals(listOf(moment), lockRequests)
    }

    @Test
    fun `failing with attempts left plans the next attempt after the backoff`() {
        val failed = service.fail(processing, cause)

        assertEquals(DeliveryStatus.FAILED, failed.delivery.status)
        assertEquals(moment.plusSeconds(30), failed.delivery.scheduledAt)
    }

    @Test
    fun `completing records the current time`() {
        val completed = service.complete(processing)

        assertEquals(DeliveryStatus.COMPLETED, completed.delivery.status)
        assertEquals(moment, completed.delivery.terminatedAt)
    }

    @Test
    fun `failing without attempts left cancels the delivery and stores the cause`() {
        val canceled = createService(maxAttempts = 1).fail(processing, cause)

        assertEquals(DeliveryStatus.CANCELED, canceled.delivery.status)
        assertEquals("java.lang.IllegalStateException: timeout", canceled.delivery.error)
        assertEquals(moment, canceled.delivery.terminatedAt)
    }

    @Test
    fun `recovering looks for records processing longer than the staleness`() {
        service.recoverStale(10)

        assertEquals(listOf(moment.minusSeconds(600) to 10), staleLookups)
    }

    @Test
    fun `recovering a stale record with attempts left plans the next attempt after the backoff`() {
        staleOutboxes += stale

        val recovered = service.recoverStale(10)

        assertEquals(listOf(stale), recovered)
        assertEquals(listOf(stale), savedOutboxes)
        assertEquals(DeliveryStatus.FAILED, stale.delivery.status)
        assertEquals(1, stale.delivery.attempts)
        assertEquals(moment.plusSeconds(30), stale.delivery.scheduledAt)
        assertEquals(
            "pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryTimeoutException: " +
                "Delivery started at 2026-11-08T23:00:00Z has not finished within PT10M",
            stale.delivery.error,
        )
    }

    @Test
    fun `recovering a stale record without attempts left cancels the delivery`() {
        staleOutboxes += stale

        createService(maxAttempts = 1).recoverStale(10)

        assertEquals(DeliveryStatus.CANCELED, stale.delivery.status)
        assertEquals(moment, stale.delivery.terminatedAt)
    }

    @Test
    fun `recovering skips a record changed by another process`() {
        val changed = createProcessingOutbox(moment.minusSeconds(3600), moment.minusSeconds(3600))
        staleOutboxes += listOf(changed, stale)
        saveFailures += OutboxOptimisticLockException(changed.id)

        val recovered = service.recoverStale(10)

        assertEquals(listOf(stale), recovered)
        assertEquals(listOf(stale), savedOutboxes)
    }

    @Test
    fun `recovering without stale records changes nothing`() {
        val recovered = service.recoverStale(10)

        assertTrue(recovered.isEmpty())
        assertTrue(savedOutboxes.isEmpty())
    }
}
