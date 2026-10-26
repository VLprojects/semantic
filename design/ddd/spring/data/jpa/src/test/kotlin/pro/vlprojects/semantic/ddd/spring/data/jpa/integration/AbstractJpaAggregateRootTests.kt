package pro.vlprojects.semantic.ddd.spring.data.jpa.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Database.runInTransaction
import java.util.concurrent.atomic.AtomicLong

class AbstractJpaAggregateRootTests {

    private val cartId = CartId(sequence.incrementAndGet())

    @Test
    fun `the identifier and the version are the only stored fields of the base`() {
        val attributes = runInTransaction {
            this.metamodel
                .entity(Cart::class.java)
                .attributes
                .map { it.name }
                .toSet()
        }

        assertEquals(setOf("identifier", "version", "items"), attributes)
    }

    @Test
    fun `an aggregate is found by its identifier`() {
        runInTransaction { persist(Cart(cartId).add()) }

        val found = runInTransaction { find(Cart::class.java, cartId) }

        assertEquals(cartId, found.identifier)
        assertEquals(1, found.items)
    }

    @Test
    fun `an aggregate is new until it is persisted`() {
        val cart = Cart(cartId)

        assertTrue(cart.isNew)
        runInTransaction { persist(cart) }
        assertFalse(cart.isNew)
    }

    @Test
    fun `a loaded aggregate is not new`() {
        runInTransaction { persist(Cart(cartId)) }

        val found = runInTransaction { find(Cart::class.java, cartId) }

        assertFalse(found.isNew)
    }

    @Test
    fun `the version grows with every change`() {
        runInTransaction { persist(Cart(cartId)) }

        runInTransaction { find(Cart::class.java, cartId).add() }

        assertEquals(1, runInTransaction { find(Cart::class.java, cartId).version })
    }

    @Test
    fun `an aggregate equals its proxy`() {
        runInTransaction { persist(Cart(cartId)) }

        val found = runInTransaction { find(Cart::class.java, cartId) }

        runInTransaction {
            val proxy = getReference(Cart::class.java, cartId)

            assertNotSame(Cart::class.java, proxy.javaClass)
            assertEquals(found, proxy)
            assertEquals(proxy, found)
        }
    }

    companion object {

        private val sequence = AtomicLong()
    }
}
