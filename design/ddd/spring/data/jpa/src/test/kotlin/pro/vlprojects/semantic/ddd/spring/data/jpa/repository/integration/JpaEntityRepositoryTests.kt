package pro.vlprojects.semantic.ddd.spring.data.jpa.repository.integration

import com.vladmihalcea.sql.SQLStatementCountValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Database.runInTransaction
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Item
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.ItemId
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
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

    @Test
    fun `an entity is found for update in one select`() {
        runInTransaction { persist(Item(itemId)) }

        SQLStatementCountValidator.reset()
        val found = runInTransaction { createItems().findForUpdateByIdOrThrow(itemId) }
        SQLStatementCountValidator.assertSelectCount(1)

        assertEquals(itemId, found.identifier)
    }

    @Test
    fun `an entity that is not stored is not found for update`() {
        SQLStatementCountValidator.reset()

        val error = assertThrows<NotFoundException> {
            runInTransaction { createItems().findForUpdateByIdOrThrow(itemId) }
        }

        SQLStatementCountValidator.assertSelectCount(1)
        assertEquals("Entity not found [id: $itemId]", error.message)
    }

    @Test
    fun `a transaction waits for the lock another one holds`() {
        runInTransaction { persist(Item(itemId)) }
        val locked = CountDownLatch(1)
        val released = CountDownLatch(1)

        Executors.newFixedThreadPool(2).use { executor ->
            val first = executor.submit {
                runInTransaction {
                    createItems().findForUpdateByIdOrThrow(itemId)
                    locked.countDown()
                    released.await(10, SECONDS)
                }
            }
            val isLocked = locked.await(10, SECONDS)
            assertTrue(isLocked)

            val second = executor.submit<Item> {
                runInTransaction { createItems().findForUpdateByIdOrThrow(itemId) }
            }
            assertThrows<TimeoutException> { second.get(500, MILLISECONDS) }

            released.countDown()
            first.get(10, SECONDS)
            val found = second.get(10, SECONDS)

            assertEquals(itemId, found.identifier)
        }
    }

    private fun EntityManager.createItems() = JpaRepositoryFactory(this).getRepository(ItemRepository::class.java)

    companion object {

        // Above the keys the tests of the bases store in the same table.
        private val sequence = AtomicLong(1_000_000)
    }
}
