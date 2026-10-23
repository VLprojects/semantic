package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision

class BaseRevisionTests {

    @Test
    fun `an aggregate gives a revision of its identifier and version`() {
        assertEquals(BaseRevision("cart", CartId(1), 3), Cart(CartId(1), version = 3).revision)
    }

    @Test
    fun `a revision names the aggregate after its class in kebab case`() {
        assertEquals("wish-list", WishList(CartId(1)).revision.name)
    }

    @Test
    fun `a revision shows its name, identifier and version`() {
        assertEquals("[name: cart, id: 1, version: 3]", Cart(CartId(1), version = 3).revision.toString())
    }
}
