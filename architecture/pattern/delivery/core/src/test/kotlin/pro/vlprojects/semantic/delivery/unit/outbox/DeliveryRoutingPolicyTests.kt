package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.BroadcastDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination
import pro.vlprojects.semantic.delivery.outbox.DeliveryRoute
import pro.vlprojects.semantic.delivery.outbox.FallbackDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxDeliverer
import pro.vlprojects.semantic.delivery.outbox.UniqueDeliveryRoutingPolicy
import pro.vlprojects.semantic.delivery.outbox.error.NoUniqueOutboxDelivererException
import pro.vlprojects.semantic.delivery.outbox.error.OutboxDelivererNotFoundException

class DeliveryRoutingPolicyTests {

    private val deliverer = object : OutboxDeliverer<TestDestination> {

        override fun supports(destination: DeliveryDestination) = destination is TestDestination

        override fun send(outbox: Outbox, destination: TestDestination) = Unit
    }
    private val route = DeliveryRoute.of(deliverer, TestDestination)
    private val otherRoute = DeliveryRoute.of(deliverer, TestDestination)

    @Test
    fun `unique keeps the only route`() {
        assertEquals(listOf(route), UniqueDeliveryRoutingPolicy().select(TestDestination, listOf(route)))
    }

    @Test
    fun `unique fails without routes`() {
        assertThrows(OutboxDelivererNotFoundException::class.java) {
            UniqueDeliveryRoutingPolicy().select(TestDestination, emptyList())
        }
    }

    @Test
    fun `unique fails with several routes`() {
        assertThrows(NoUniqueOutboxDelivererException::class.java) {
            UniqueDeliveryRoutingPolicy().select(TestDestination, listOf(route, otherRoute))
        }
    }

    @Test
    fun `broadcast keeps every route`() {
        val routes = listOf(route, otherRoute)

        assertEquals(routes, BroadcastDeliveryRoutingPolicy().select(TestDestination, routes))
    }

    @Test
    fun `broadcast fails without routes`() {
        assertThrows(OutboxDelivererNotFoundException::class.java) {
            BroadcastDeliveryRoutingPolicy().select(TestDestination, emptyList())
        }
    }

    @Test
    fun `fallback gives its route when there are no routes`() {
        assertEquals(listOf(route), FallbackDeliveryRoutingPolicy(route).select(TestDestination, emptyList()))
    }

    @Test
    fun `fallback keeps the found routes`() {
        assertEquals(
            listOf(otherRoute),
            FallbackDeliveryRoutingPolicy(route).select(TestDestination, listOf(otherRoute)),
        )
    }

    private data object TestDestination : DeliveryDestination
}
