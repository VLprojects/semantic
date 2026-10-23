package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.base.Revision
import pro.vlprojects.semantic.ddd.tactical.base.SimpleEventHeader
import pro.vlprojects.semantic.ddd.tactical.base.UuidEventId

class AbstractEventTests {
    private val revision = Revision("cart", CartId(1), 0)

    @Test
    fun `events with one id are equal`() {
        val header = SimpleEventHeader(UuidEventId.random(), "cart.item-added", Cart.NOW)

        assertEquals(ItemAdded(header, revision, "apple"), ItemAdded(header, revision, "pear"))
    }

    @Test
    fun `events with different ids differ`() {
        val first = SimpleEventHeader(UuidEventId.random(), "cart.item-added", Cart.NOW)
        val second = SimpleEventHeader(UuidEventId.random(), "cart.item-added", Cart.NOW)

        assertNotEquals(ItemAdded(first, revision, "apple"), ItemAdded(second, revision, "apple"))
    }
}
