package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision
import pro.vlprojects.semantic.ddd.tactical.base.UuidEventId
import java.util.UUID

class AbstractEventTests {

    private val cart = Cart(CartId(1))
    private val id = UuidEventId(UUID.fromString("00000000-0000-0000-0000-000000000001"))

    @Test
    fun `events with one id are equal`() {
        assertEquals(ItemAdded.of(cart, "apple", id), ItemAdded.of(cart, "pear", id))
    }

    @Test
    fun `events with different ids differ`() {
        assertNotEquals(ItemAdded.of(cart, "apple"), ItemAdded.of(cart, "apple"))
    }

    @Test
    fun `an event raised by an aggregate gets a header named after its class and the revision`() {
        val event = ItemAdded.of(Cart(CartId(1), version = 3), "apple", id, Cart.NOW)

        assertEquals(id, event.header.eventId)
        assertEquals("item-added", event.header.name)
        assertEquals(Cart.NOW, event.header.occurredAt)
        assertEquals(BaseRevision("cart", CartId(1), 3), event.aggregateRevision)
    }

    @Test
    fun `an event shows its class, abbreviated id and revision`() {
        assertEquals(
            "ItemAdded [eventId: 000…01, aggregate: [name: cart, id: 1, version: 0]]",
            ItemAdded.of(cart, "apple", id).toString(),
        )
    }
}
