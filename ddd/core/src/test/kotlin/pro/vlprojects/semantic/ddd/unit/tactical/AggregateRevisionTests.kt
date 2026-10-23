package pro.vlprojects.semantic.ddd.unit.tactical

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.unit.tactical.base.Cart
import pro.vlprojects.semantic.ddd.unit.tactical.base.CartId
import pro.vlprojects.semantic.ddd.unit.tactical.base.WishList

class AggregateRevisionTests {

    @Test
    fun `a revision takes the identifier and the version of the aggregate`() {
        assertEquals(AggregateRevision("cart", CartId(1), 3), AggregateRevision.of(Cart(CartId(1), version = 3)))
    }

    @Test
    fun `an aggregate is named after its class in kebab case`() {
        assertEquals("wish-list", AggregateRevision.of(WishList(CartId(1))).name)
    }

    @Test
    fun `a revision shows its name, identifier and version`() {
        assertEquals(
            "[name: cart, id: 1, version: 3]",
            AggregateRevision.of(Cart(CartId(1), version = 3)).toString(),
        )
    }
}
