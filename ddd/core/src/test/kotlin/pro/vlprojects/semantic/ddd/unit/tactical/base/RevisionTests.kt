package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.base.Revision

class RevisionTests {
    @Test
    fun `a revision takes the identifier and the version of the aggregate`() {
        assertEquals(Revision("cart", CartId(1), 3), Revision.of(Cart(CartId(1), version = 3)))
    }

    @Test
    fun `an aggregate is named after its class in kebab case`() {
        assertEquals("wish-list", Revision.of(WishList(CartId(1))).name)
    }
}
