package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.BroadcastDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.FallbackDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.UniqueDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.error.NoUniqueOutboxDelivererException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotFoundException

class DeliveryRoutingPolicyTests {

    private val deliverer = AnyDeliverer()
    private val otherDeliverer = AnyDeliverer()

    @Test
    fun `unique keeps the only deliverer`() {
        assertEquals(listOf(deliverer), UniqueDeliveryRoutingPolicy().select(TestDestination, listOf(deliverer)))
    }

    @Test
    fun `unique fails without deliverers`() {
        assertThrows(OutboxDelivererNotFoundException::class.java) {
            UniqueDeliveryRoutingPolicy().select(TestDestination, emptyList())
        }
    }

    @Test
    fun `unique fails with several deliverers`() {
        assertThrows(NoUniqueOutboxDelivererException::class.java) {
            UniqueDeliveryRoutingPolicy().select(TestDestination, listOf(deliverer, otherDeliverer))
        }
    }

    @Test
    fun `broadcast keeps every deliverer`() {
        val deliverers = listOf(deliverer, otherDeliverer)

        assertEquals(deliverers, BroadcastDeliveryRoutingPolicy().select(TestDestination, deliverers))
    }

    @Test
    fun `broadcast fails without deliverers`() {
        assertThrows(OutboxDelivererNotFoundException::class.java) {
            BroadcastDeliveryRoutingPolicy().select(TestDestination, emptyList())
        }
    }

    @Test
    fun `fallback gives its deliverer when none was found`() {
        assertEquals(listOf(deliverer), FallbackDeliveryRoutingPolicy(deliverer).select(TestDestination, emptyList()))
    }

    @Test
    fun `fallback keeps the found deliverers`() {
        val deliverers = listOf(otherDeliverer)

        assertEquals(deliverers, FallbackDeliveryRoutingPolicy(deliverer).select(TestDestination, deliverers))
    }

    private data object TestDestination : DeliveryDestination

    private class AnyDeliverer : OutboxDeliverer<DeliveryDestination> {

        override fun supports(destination: DeliveryDestination) = true

        override fun send(outbox: Outbox, destination: DeliveryDestination) = Unit
    }
}
