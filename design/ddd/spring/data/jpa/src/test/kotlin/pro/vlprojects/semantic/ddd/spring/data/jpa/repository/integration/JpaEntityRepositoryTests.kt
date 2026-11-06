package pro.vlprojects.semantic.ddd.spring.data.jpa.repository.integration

import com.vladmihalcea.sql.SQLStatementCountValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Database.runInTransaction
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Item
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.ItemId
import java.util.concurrent.atomic.AtomicLong
import jakarta.persistence.EntityManager

class JpaEntityRepositoryTests {

    private val itemId = ItemId(sequence.incrementAndGet())

    @Test
    fun `an entity is found by its identifier`() {
        SQLStatementCountValidator.reset()
        runInTransaction { persist(Item(itemId)) }
        SQLStatementCountValidator.assertInsertCount(1)

        SQLStatementCountValidator.reset()
        val found = runInTransaction { createItems().findByIdOrThrow(itemId) }
        SQLStatementCountValidator.assertSelectCount(1)

        assertEquals(itemId, found.identifier)
    }

    @Test
    fun `an entity that is not stored is not found`() {
        SQLStatementCountValidator.reset()

        val error = assertThrows<NotFoundException> {
            runInTransaction { createItems().findByIdOrThrow(itemId) }
        }

        SQLStatementCountValidator.assertSelectCount(1)
        assertEquals("Entity not found [id: $itemId]", error.message)
    }

    private fun EntityManager.createItems() = JpaRepositoryFactory(this).getRepository(ItemRepository::class.java)

    companion object {

        // Above the keys the tests of the bases store in the same table.
        private val sequence = AtomicLong(1_000_000)
    }
}
