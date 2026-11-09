package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.OutboxRouter
import pro.vlprojects.semantic.delivery.outbox.destination.Destination
import pro.vlprojects.semantic.delivery.outbox.destination.DestinationResolver
import java.time.Instant

class OutboxRouterTests {

    private val outbox = "2026-11-09T00:00:00Z"
        .let(Instant::parse)
        .let(::createOutbox)
    private val brokerDestination = BrokerDestination("orders")
    private val brokerDeliverer = BrokerDeliverer()
    private val localDeliverer = LocalDeliverer()

    @Test
    fun `route hands the outbox to the deliverer of the resolved destination`() {
        val router = OutboxRouter({ brokerDestination }, listOf(localDeliverer, brokerDeliverer))

        router.route(outbox)

        assertEquals(listOf(outbox to brokerDestination), brokerDeliverer.delivered)
        assertTrue(localDeliverer.delivered.isEmpty())
    }

    @Test
    fun `route resolves the destination from the context of the outbox`() {
        val contexts = mutableListOf<Any>()
        val resolver = DestinationResolver { context ->
            contexts += context
            brokerDestination
        }

        OutboxRouter(resolver, listOf(brokerDeliverer)).route(outbox)

        assertSame(outbox.context, contexts.single())
    }

    @Test
    fun `route fails when no deliverer accepts the destination`() {
        val router = OutboxRouter({ brokerDestination }, listOf(localDeliverer))

        assertThrows(IllegalStateException::class.java) { router.route(outbox) }
    }

    @Test
    fun `route propagates the failure of the deliverer`() {
        val failure = IllegalArgumentException("refused")
        val deliverer = object : OutboxDeliverer<BrokerDestination> {

            override val destinationType = BrokerDestination::class

            override fun deliver(outbox: Outbox, destination: BrokerDestination) = throw failure
        }
        val router = OutboxRouter({ brokerDestination }, listOf(deliverer))

        assertSame(failure, assertThrows(IllegalArgumentException::class.java) { router.route(outbox) })
    }

    @Test
    fun `a router rejects two deliverers of one destination type`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxRouter({ brokerDestination }, listOf(BrokerDeliverer(), BrokerDeliverer()))
        }
    }

    private data class BrokerDestination(val exchange: String) : Destination

    private data object LocalDestination : Destination

    private class BrokerDeliverer : OutboxDeliverer<BrokerDestination> {

        val delivered = mutableListOf<Pair<Outbox, BrokerDestination>>()

        override val destinationType = BrokerDestination::class

        override fun deliver(outbox: Outbox, destination: BrokerDestination) {
            delivered += outbox to destination
        }
    }

    private class LocalDeliverer : OutboxDeliverer<LocalDestination> {

        val delivered = mutableListOf<Outbox>()

        override val destinationType = LocalDestination::class

        override fun deliver(outbox: Outbox, destination: LocalDestination) {
            delivered += outbox
        }
    }
}
