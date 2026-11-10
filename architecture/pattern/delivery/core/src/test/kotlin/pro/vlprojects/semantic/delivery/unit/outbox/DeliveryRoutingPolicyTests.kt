package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.FallbackDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.FirstDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.LastDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.UniqueDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.error.MultipleOutboxDeliverersException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotResolvedException

class DeliveryRoutingPolicyTests {

    private val deliverer = AnyDeliverer()
    private val otherDeliverer = AnyDeliverer()

    @Test
    fun `unique keeps the only deliverer`() {
        assertEquals(deliverer, UniqueDeliveryRoutingPolicy().select(TestDestination, listOf(deliverer)))
    }

    @Test
    fun `unique fails without deliverers`() {
        assertThrows(OutboxDelivererNotResolvedException::class.java) {
            UniqueDeliveryRoutingPolicy().select(TestDestination, emptyList())
        }
    }

    @Test
    fun `unique fails with several deliverers`() {
        assertThrows(MultipleOutboxDeliverersException::class.java) {
            UniqueDeliveryRoutingPolicy().select(TestDestination, listOf(deliverer, otherDeliverer))
        }
    }

    @Test
    fun `first selects the first deliverer`() {
        val selected = FirstDeliveryRoutingPolicy().select(TestDestination, listOf(deliverer, otherDeliverer))

        assertEquals(deliverer, selected)
    }

    @Test
    fun `first fails without deliverers`() {
        assertThrows(OutboxDelivererNotResolvedException::class.java) {
            FirstDeliveryRoutingPolicy().select(TestDestination, emptyList())
        }
    }

    @Test
    fun `last selects the last deliverer`() {
        val selected = LastDeliveryRoutingPolicy().select(TestDestination, listOf(deliverer, otherDeliverer))

        assertEquals(otherDeliverer, selected)
    }

    @Test
    fun `last fails without deliverers`() {
        assertThrows(OutboxDelivererNotResolvedException::class.java) {
            LastDeliveryRoutingPolicy().select(TestDestination, emptyList())
        }
    }

    @Test
    fun `fallback gives its deliverer when none was found`() {
        assertEquals(deliverer, FallbackDeliveryRoutingPolicy(deliverer).select(TestDestination, emptyList()))
    }

    @Test
    fun `fallback keeps the found deliverer`() {
        val selected = FallbackDeliveryRoutingPolicy(deliverer).select(TestDestination, listOf(otherDeliverer))

        assertEquals(otherDeliverer, selected)
    }

    @Test
    fun `fallback fails with several deliverers`() {
        assertThrows(MultipleOutboxDeliverersException::class.java) {
            FallbackDeliveryRoutingPolicy(deliverer).select(TestDestination, listOf(deliverer, otherDeliverer))
        }
    }

    private data object TestDestination : DeliveryDestination

    private class AnyDeliverer : OutboxDeliverer<DeliveryDestination> {

        override fun supports(destination: DeliveryDestination) = true

        override fun send(outbox: Outbox, destination: DeliveryDestination) = Unit
    }
}
