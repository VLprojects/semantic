package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.EventMeta
import pro.vlprojects.semantic.ddd.tactical.base.EventId
import java.util.UUID

class AbstractEventTests {

    private val cart = Cart(CartId(1))
    private val meta = UUID
        .fromString("00000000-0000-0000-0000-000000000001")
        .let(::EventId)
        .let { EventMeta.of<ItemAdded>(it, Cart.NOW) }

    @Test
    fun `events with one id are equal`() {
        assertEquals(ItemAdded.of(cart, "apple", meta), ItemAdded.of(cart, "pear", meta))
    }

    @Test
    fun `events with different ids differ`() {
        assertNotEquals(ItemAdded.of(cart, "apple"), ItemAdded.of(cart, "apple"))
    }

    @Test
    fun `an event raised by an aggregate takes the revision of the aggregate`() {
        val event = ItemAdded.of(Cart(CartId(1), version = 3), "apple", meta)

        assertEquals(meta, event.meta)
        assertEquals(AggregateRevision("cart", CartId(1), 3), event.aggregateRevision)
    }

    @Test
    fun `an event meta is named after the class of the event`() {
        assertEquals("item-added", ItemAdded.of(cart, "apple").meta.eventName)
    }

    @Test
    fun `an event shows its class, abbreviated id and revision`() {
        assertEquals(
            "ItemAdded [eventId: 000…01, aggregate: [name: cart, id: 1, version: 0]]",
            ItemAdded.of(cart, "apple", meta).toString(),
        )
    }
}
