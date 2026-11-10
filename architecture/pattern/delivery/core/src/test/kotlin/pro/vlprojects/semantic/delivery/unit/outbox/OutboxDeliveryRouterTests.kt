package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.BroadcastDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.DeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.FallbackDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliveryRouter
import pro.vlprojects.semantic.delivery.outbox.error.MultipleOutboxDeliverersException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotResolvedException

class OutboxDeliveryRouterTests {

    private val brokerDestination = BrokerDestination("orders")
    private val brokerDeliverer = BrokerDeliverer()
    private val localDeliverer = LocalDeliverer()

    @Test
    fun `determine returns the deliverer that supports the destination`() {
        val router = OutboxDeliveryRouter(listOf(localDeliverer, brokerDeliverer))

        assertEquals(listOf(brokerDeliverer), router.determine(brokerDestination))
    }

    @Test
    fun `determine fails when several deliverers support the destination`() {
        val router = OutboxDeliveryRouter(listOf(brokerDeliverer, BrokerDeliverer()))

        assertThrows(MultipleOutboxDeliverersException::class.java) { router.determine(brokerDestination) }
    }

    @Test
    fun `determine fails when no deliverer supports the destination`() {
        val router = OutboxDeliveryRouter(listOf(localDeliverer))

        assertThrows(OutboxDelivererNotResolvedException::class.java) { router.determine(brokerDestination) }
    }

    @Test
    fun `determine returns only the deliverers the policy selects`() {
        val policy = DeliveryRoutingPolicy { _, _ -> emptyList() }
        val router = OutboxDeliveryRouter(listOf(brokerDeliverer), policy)

        assertTrue(router.determine(brokerDestination).isEmpty())
    }

    @Test
    fun `determine returns every deliverer the policy selects`() {
        val second = BrokerDeliverer()
        val router = OutboxDeliveryRouter(listOf(brokerDeliverer, second), BroadcastDeliveryRoutingPolicy())

        assertEquals(listOf(brokerDeliverer, second), router.determine(brokerDestination))
    }

    @Test
    fun `determine returns the fallback deliverer for an unsupported destination`() {
        val fallback = object : OutboxDeliverer<DeliveryDestination> {

            override fun supports(destination: DeliveryDestination) = false

            override fun send(outbox: Outbox, destination: DeliveryDestination) = Unit
        }
        val router = OutboxDeliveryRouter(listOf(localDeliverer), FallbackDeliveryRoutingPolicy(fallback))

        assertEquals(listOf(fallback), router.determine(brokerDestination))
    }

    private data class BrokerDestination(val exchange: String) : DeliveryDestination

    private data object LocalDestination : DeliveryDestination

    private class BrokerDeliverer : OutboxDeliverer<BrokerDestination> {

        override fun supports(destination: DeliveryDestination) = destination is BrokerDestination

        override fun send(outbox: Outbox, destination: BrokerDestination) = Unit
    }

    private class LocalDeliverer : OutboxDeliverer<LocalDestination> {

        override fun supports(destination: DeliveryDestination) = destination is LocalDestination

        override fun send(outbox: Outbox, destination: LocalDestination) = Unit
    }
}
