package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.EventPublisher

class EventPublisherTests {

    @Test
    fun `events are published one by one in their order`() {
        val published = mutableListOf<String>()

        EventPublisher<String>(published::add).publish(listOf("placed", "paid"))

        assertEquals(listOf("placed", "paid"), published)
    }
}
