package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.base.BaseEventHeader
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision
import pro.vlprojects.semantic.ddd.tactical.base.UuidEventId
import java.util.UUID

class AbstractEventTests {

    private val revision = BaseRevision("cart", CartId(1), 0)

    @Test
    fun `events with one id are equal`() {
        val header = BaseEventHeader(UuidEventId.random(), "item-added", Cart.NOW)

        assertEquals(ItemAdded(header, revision, "apple"), ItemAdded(header, revision, "pear"))
    }

    @Test
    fun `events with different ids differ`() {
        val first = BaseEventHeader(UuidEventId.random(), "item-added", Cart.NOW)
        val second = BaseEventHeader(UuidEventId.random(), "item-added", Cart.NOW)

        assertNotEquals(ItemAdded(first, revision, "apple"), ItemAdded(second, revision, "apple"))
    }

    @Test
    fun `an event raised by an aggregate gets a header named after its class and the revision`() {
        val event = ItemAdded.of(Cart(CartId(1), version = 3), "apple", Cart.NOW)

        assertEquals("item-added", event.header.name)
        assertEquals(Cart.NOW, event.header.occurredAt)
        assertEquals(BaseRevision("cart", CartId(1), 3), event.aggregateRevision)
    }

    @Test
    fun `an event shows its class, abbreviated id and revision`() {
        val id = UuidEventId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val event = ItemAdded(BaseEventHeader(id, "item-added", Cart.NOW), revision, "apple")

        assertEquals("ItemAdded [eventId: 000…01, aggregate: [name: cart, id: 1, version: 0]]", event.toString())
    }
}
