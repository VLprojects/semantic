package pro.vlprojects.semantic.ddd.spring.data.mongodb.repository.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Cart
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.CartId
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Database.template
import java.util.concurrent.atomic.AtomicLong

class MongoAggregateRepositoryTests {

    private val cartId = CartId(sequence.incrementAndGet())

    private val carts = MongoRepositoryFactory(template).getRepository(CartRepository::class.java)

    @Test
    fun `an aggregate is saved and found by its identifier`() {
        carts.save(Cart(cartId).add())

        val found = carts.findByIdOrThrow(cartId)

        assertEquals(cartId, found.identifier)
        assertEquals(1, found.items)
    }

    @Test
    fun `an aggregate that is not stored is not found`() {
        val error = assertThrows<NotFoundException> { carts.findByIdOrThrow(cartId) }

        assertEquals("Entity not found [id: $cartId]", error.message)
    }

    companion object {

        // Above the keys the tests of the bases store in the same collection.
        private val sequence = AtomicLong(1_000_000)
    }
}
