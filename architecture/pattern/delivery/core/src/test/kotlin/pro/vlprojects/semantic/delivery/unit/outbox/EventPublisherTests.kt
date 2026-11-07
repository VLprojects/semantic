package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.EventPublisher

class EventPublisherTests {

    @Test
    fun `events are published one by one in their order`() {
        val published = mutableListOf<OrderPlaced>()
        val events = listOf(OrderId(1), OrderId(2))
            .map(::Order)
            .map(::OrderPlaced)

        EventPublisher<OrderPlaced>(published::add).publish(events)

        assertEquals(events, published)
    }
}
