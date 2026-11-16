package pro.vlprojects.semantic.ddd.spring.data.mongodb.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.mongodb.core.findById
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Database.mappingContext
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Database.template
import java.util.concurrent.atomic.AtomicLong

class AbstractMongoAggregateRootTests {

    private val cartId = CartId(sequence.incrementAndGet())

    @Test
    fun `the identifier and the version are the only stored fields of the base`() {
        val fields = mappingContext
            .getRequiredPersistentEntity(Cart::class.java)
            .map { it.fieldName }
            .toSet()

        assertEquals(setOf("_id", "version", "items"), fields)
    }

    @Test
    fun `an aggregate is found by its identifier`() {
        template.save(Cart(cartId).add())

        val found = template.findById<Cart>(cartId)

        assertEquals(cartId, found?.identifier)
        assertEquals(1, found?.items)
    }

    @Test
    fun `the version starts at one and grows with every save`() {
        val cart = Cart(cartId)

        template.save(cart)
        assertEquals(1, cart.version)
        template.save(cart.add())

        assertEquals(2, template.findById<Cart>(cartId)?.version)
    }

    @Test
    fun `a stale copy is not saved`() {
        template.save(Cart(cartId))
        val first = template.findById<Cart>(cartId)!!
        val second = template.findById<Cart>(cartId)!!

        template.save(first.add())

        assertThrows<OptimisticLockingFailureException> { template.save(second.add()) }
    }

    companion object {

        private val sequence = AtomicLong()
    }
}
