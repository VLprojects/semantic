package pro.vlprojects.semantic.ddd.spring.data.jpa.repository.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Cart
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.CartId
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Database.runInTransaction
import java.util.concurrent.atomic.AtomicLong
import jakarta.persistence.EntityManager

class JpaAggregateRepositoryTests {

    private val cartId = CartId(sequence.incrementAndGet())

    @Test
    fun `an aggregate is saved and found by its identifier`() {
        runInTransaction { createCarts().save(Cart(cartId)) }

        val found = runInTransaction { createCarts().findByIdOrThrow(cartId) }

        assertEquals(cartId, found.identifier)
    }

    @Test
    fun `an aggregate that is not stored is not found`() {
        val error = assertThrows<NotFoundException> {
            runInTransaction { createCarts().findByIdOrThrow(cartId) }
        }

        assertEquals("Entity not found [id: $cartId]", error.message)
    }

    private fun EntityManager.createCarts() = JpaRepositoryFactory(this).getRepository(CartRepository::class.java)

    companion object {

        // Above the keys the tests of the bases store in the same table.
        private val sequence = AtomicLong(1_000_000)
    }
}
