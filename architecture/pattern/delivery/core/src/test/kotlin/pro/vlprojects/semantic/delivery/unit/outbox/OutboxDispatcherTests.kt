package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestinationDeserializer
import pro.vlprojects.semantic.delivery.outbox.DeliveryStatus
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliveryRouter
import pro.vlprojects.semantic.delivery.outbox.OutboxDispatcher
import pro.vlprojects.semantic.delivery.outbox.OutboxProperties
import pro.vlprojects.semantic.delivery.outbox.OutboxRepository
import pro.vlprojects.semantic.delivery.outbox.OutboxService
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryRejectedException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxOptimisticLockException
import pro.vlprojects.semantic.delivery.outbox.id.OutboxId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OutboxDispatcherTests {

    private val moment = Instant.parse("2026-11-09T00:00:00Z")
    private val clock = Clock.fixed(moment, ZoneOffset.UTC)
    private val lockedUntil = moment.plusSeconds(60)
    private val config = OutboxProperties(batchSize = 2)
    private val lockedOutboxes = mutableListOf<Outbox>()
    private val requestedLimits = mutableListOf<Int>()
    private val savedStatuses = mutableListOf<DeliveryStatus>()
    private val sent = mutableListOf<Pair<Outbox, DeliveryDestination>>()
    private val deserialized = mutableListOf<String>()
    private val deliveryFailures = ArrayDeque<Exception>()
    private val destinationFailures = ArrayDeque<Exception>()
    private val saveFailures = ArrayDeque<RuntimeException>()

    private val repository = object : OutboxRepository {

        override fun save(outbox: Outbox): Outbox {
            saveFailures.removeFirstOrNull()?.let { failure -> throw failure }
            savedStatuses += outbox.delivery.status

            return outbox
        }

        override fun findById(id: OutboxId) = null

        override fun lockScheduledForDelivery(
            scheduledUntil: Instant,
            lockedUntil: Instant,
            limit: Int,
        ) = lockedOutboxes.also { requestedLimits += limit }

        override fun deleteAll(status: DeliveryStatus, terminatedBefore: Instant, limit: Int) = 0
    }

    private val deliverer = object : OutboxDeliverer<DeliveryDestination> {

        override fun supports(destination: DeliveryDestination) = true

        override fun send(outbox: Outbox, destination: DeliveryDestination) {
            sent += outbox to destination
            deliveryFailures.removeFirstOrNull()?.let { failure -> throw failure }
        }
    }

    private val service = OutboxService(
        repository,
        { byteArrayOf(1) },
        emptyList<Nothing>().let(::ContextCollector),
        { "orders" },
        { OutboxId.of("generated") },
        config,
        clock,
    )

    private val destination = object : DeliveryDestination {}

    private val deserializer = DeliveryDestinationDeserializer { stored ->
        deserialized += stored
        destinationFailures.removeFirstOrNull()?.let { failure -> throw failure }

        destination
    }

    private val router = listOf(deliverer).let(::OutboxDeliveryRouter)
    private val dispatcher = OutboxDispatcher(service, deserializer, router, config)

    private fun lockOutboxes(count: Int) {
        repeat(count) { lockedOutboxes += createStartedOutbox(moment, lockedUntil) }
    }

    @Test
    fun `dispatch completes a delivered outbox`() {
        lockOutboxes(1)

        dispatcher.dispatch()

        assertEquals(listOf(DeliveryStatus.COMPLETED), savedStatuses)
    }

    @Test
    fun `dispatch sends the outbox to the destination stored in it`() {
        lockOutboxes(1)

        dispatcher.dispatch()

        assertEquals(lockedOutboxes.map(Outbox::destination), deserialized)
        assertEquals(lockedOutboxes.map { outbox -> outbox to destination }, sent)
    }

    @Test
    fun `dispatch asks for a batch of the configured size`() {
        dispatcher.dispatch()

        assertEquals(listOf(2), requestedLimits)
    }

    @Test
    fun `dispatch returns the number of taken records`() {
        lockOutboxes(2)

        assertEquals(2, dispatcher.dispatch())
    }

    @Test
    fun `dispatch fails the outbox and goes on with the batch`() {
        lockOutboxes(2)
        deliveryFailures += OutboxDeliveryRejectedException("nack")

        dispatcher.dispatch()

        assertEquals(listOf(DeliveryStatus.FAILED, DeliveryStatus.COMPLETED), savedStatuses)
    }

    @Test
    fun `dispatch fails the outbox when its destination cannot be read`() {
        lockOutboxes(1)
        destinationFailures += IllegalArgumentException("unknown destination")

        dispatcher.dispatch()

        assertEquals(listOf(DeliveryStatus.FAILED), savedStatuses)
    }

    @Test
    fun `dispatch does not fail a delivered outbox taken over by another instance`() {
        lockOutboxes(2)
        saveFailures += lockedOutboxes.first().id.let(::OutboxOptimisticLockException)

        dispatcher.dispatch()

        assertEquals(listOf(DeliveryStatus.COMPLETED), savedStatuses)
    }

    @Test
    fun `dispatch propagates a failure to save the outbox`() {
        lockOutboxes(1)
        saveFailures += IllegalStateException("database is down")

        assertThrows(IllegalStateException::class.java) { dispatcher.dispatch() }
    }
}
