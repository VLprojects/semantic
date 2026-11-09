package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestinationResolver
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliveryRouter
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDeliveryException
import java.time.Instant

class OutboxDeliveryRouterTests {

    private val outbox = "2026-11-09T00:00:00Z"
        .let(Instant::parse)
        .let(::createOutbox)
    private val brokerDestination = BrokerDestination("orders")
    private val brokerDeliverer = BrokerDeliverer()
    private val localDeliverer = LocalDeliverer()

    @Test
    fun `route hands the outbox to the deliverer of the resolved destination`() {
        val router = OutboxDeliveryRouter({ brokerDestination }, listOf(localDeliverer, brokerDeliverer))

        router.route(outbox)

        assertEquals(listOf(outbox to brokerDestination), brokerDeliverer.delivered)
        assertTrue(localDeliverer.delivered.isEmpty())
    }

    @Test
    fun `route resolves the destination from the context of the outbox`() {
        val contexts = mutableListOf<Any>()
        val resolver = DeliveryDestinationResolver { context ->
            contexts += context
            brokerDestination
        }

        OutboxDeliveryRouter(resolver, listOf(brokerDeliverer)).route(outbox)

        assertSame(outbox.context, contexts.single())
    }

    @Test
    fun `route uses the first deliverer that supports the destination`() {
        val second = BrokerDeliverer()

        OutboxDeliveryRouter({ brokerDestination }, listOf(brokerDeliverer, second)).route(outbox)

        assertEquals(1, brokerDeliverer.delivered.size)
        assertTrue(second.delivered.isEmpty())
    }

    @Test
    fun `route fails when no deliverer supports the destination`() {
        val router = OutboxDeliveryRouter({ brokerDestination }, listOf(localDeliverer))

        assertThrows(IllegalStateException::class.java) { router.route(outbox) }
    }

    @Test
    fun `route propagates the failure of the deliverer`() {
        val failure = OutboxDeliveryException("refused")
        val deliverer = object : OutboxDeliverer<BrokerDestination> {

            override fun supports(destination: DeliveryDestination) = destination is BrokerDestination

            override fun deliver(outbox: Outbox, destination: BrokerDestination) = throw failure
        }
        val router = OutboxDeliveryRouter({ brokerDestination }, listOf(deliverer))

        assertSame(failure, assertThrows(OutboxDeliveryException::class.java) { router.route(outbox) })
    }

    private data class BrokerDestination(val exchange: String) : DeliveryDestination

    private data object LocalDestination : DeliveryDestination

    private class BrokerDeliverer : OutboxDeliverer<BrokerDestination> {

        val delivered = mutableListOf<Pair<Outbox, BrokerDestination>>()

        override fun supports(destination: DeliveryDestination) = destination is BrokerDestination

        override fun deliver(outbox: Outbox, destination: BrokerDestination) {
            delivered += outbox to destination
        }
    }

    private class LocalDeliverer : OutboxDeliverer<LocalDestination> {

        val delivered = mutableListOf<Outbox>()

        override fun supports(destination: DeliveryDestination) = destination is LocalDestination

        override fun deliver(outbox: Outbox, destination: LocalDestination) {
            delivered += outbox
        }
    }
}
