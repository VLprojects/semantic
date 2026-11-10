package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.BroadcastDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestinationDeserializer
import pro.vlprojects.semantic.delivery.outbox.DeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.FallbackDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliveryRouter
import pro.vlprojects.semantic.delivery.outbox.error.MultipleOutboxDeliverersException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotResolvedException
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
    fun `send hands the outbox to the deliverer of the resolved destination`() {
        val router = OutboxDeliveryRouter({ brokerDestination }, listOf(localDeliverer, brokerDeliverer))

        router.send(outbox)

        assertEquals(listOf(outbox to brokerDestination), brokerDeliverer.delivered)
        assertTrue(localDeliverer.delivered.isEmpty())
    }

    @Test
    fun `send deserializes the destination stored in the outbox`() {
        val stored = mutableListOf<String>()
        val deserializer = DeliveryDestinationDeserializer { destination ->
            stored += destination
            brokerDestination
        }

        OutboxDeliveryRouter(deserializer, listOf(brokerDeliverer)).send(outbox)

        assertEquals(listOf(outbox.destination), stored)
    }

    @Test
    fun `send fails when several deliverers support the destination`() {
        val router = OutboxDeliveryRouter({ brokerDestination }, listOf(brokerDeliverer, BrokerDeliverer()))

        assertThrows(MultipleOutboxDeliverersException::class.java) { router.send(outbox) }
        assertTrue(brokerDeliverer.delivered.isEmpty())
    }

    @Test
    fun `send fails when no deliverer supports the destination`() {
        val router = OutboxDeliveryRouter({ brokerDestination }, listOf(localDeliverer))

        assertThrows(OutboxDelivererNotResolvedException::class.java) { router.send(outbox) }
    }

    @Test
    fun `send sends the outbox only through the routes the policy selects`() {
        val policy = DeliveryRoutingPolicy { _, _ -> emptyList() }

        OutboxDeliveryRouter({ brokerDestination }, listOf(brokerDeliverer), policy).send(outbox)

        assertTrue(brokerDeliverer.delivered.isEmpty())
    }

    @Test
    fun `send sends the outbox through every selected route`() {
        val second = BrokerDeliverer()
        val router = OutboxDeliveryRouter(
            { brokerDestination },
            listOf(brokerDeliverer, second),
            BroadcastDeliveryRoutingPolicy(),
        )

        router.send(outbox)

        assertEquals(1, brokerDeliverer.delivered.size)
        assertEquals(1, second.delivered.size)
    }

    @Test
    fun `send hands an unsupported destination to the fallback deliverer`() {
        val delivered = mutableListOf<DeliveryDestination>()
        val fallback = object : OutboxDeliverer<DeliveryDestination> {

            override fun supports(destination: DeliveryDestination) = false

            override fun send(outbox: Outbox, destination: DeliveryDestination) {
                delivered += destination
            }
        }
        val policy = FallbackDeliveryRoutingPolicy(fallback)

        OutboxDeliveryRouter({ brokerDestination }, listOf(localDeliverer), policy).send(outbox)

        assertEquals(listOf<DeliveryDestination>(brokerDestination), delivered)
    }

    @Test
    fun `send propagates the failure of the deliverer`() {
        val failure = OutboxDeliveryException("refused")
        val deliverer = object : OutboxDeliverer<BrokerDestination> {

            override fun supports(destination: DeliveryDestination) = destination is BrokerDestination

            override fun send(outbox: Outbox, destination: BrokerDestination) = throw failure
        }
        val router = OutboxDeliveryRouter({ brokerDestination }, listOf(deliverer))

        assertSame(failure, assertThrows(OutboxDeliveryException::class.java) { router.send(outbox) })
    }

    private data class BrokerDestination(val exchange: String) : DeliveryDestination

    private data object LocalDestination : DeliveryDestination

    private class BrokerDeliverer : OutboxDeliverer<BrokerDestination> {

        val delivered = mutableListOf<Pair<Outbox, BrokerDestination>>()

        override fun supports(destination: DeliveryDestination) = destination is BrokerDestination

        override fun send(outbox: Outbox, destination: BrokerDestination) {
            delivered += outbox to destination
        }
    }

    private class LocalDeliverer : OutboxDeliverer<LocalDestination> {

        val delivered = mutableListOf<Outbox>()

        override fun supports(destination: DeliveryDestination) = destination is LocalDestination

        override fun send(outbox: Outbox, destination: LocalDestination) {
            delivered += outbox
        }
    }
}
