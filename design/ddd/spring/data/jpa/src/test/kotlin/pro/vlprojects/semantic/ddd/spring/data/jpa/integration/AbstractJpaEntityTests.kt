package pro.vlprojects.semantic.ddd.spring.data.jpa.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Database.runInTransaction
import java.util.concurrent.atomic.AtomicLong

class AbstractJpaEntityTests {

    private val itemId = ItemId(sequence.incrementAndGet())

    @Test
    fun `the identifier and the version are the only stored fields of the base`() {
        val attributes = runInTransaction {
            this.metamodel
                .entity(Item::class.java)
                .attributes
                .map { it.name }
                .toSet()
        }

        assertEquals(setOf("identifier", "version"), attributes)
    }

    @Test
    fun `the key is stored in the id column`() {
        runInTransaction { persist(Item(itemId)) }

        val stored = runInTransaction {
            createNativeQuery("select id from item where id = :id", Long::class.java)
                .setParameter("id", itemId.value)
                .singleResult
        }

        assertEquals(itemId.value, stored)
    }

    @Test
    fun `an entity is new until it is persisted`() {
        val item = Item(itemId)

        assertTrue(item.isNew)
        runInTransaction { persist(item) }
        assertFalse(item.isNew)
    }

    @Test
    fun `a loaded entity is not new`() {
        runInTransaction { persist(Item(itemId)) }

        val found = runInTransaction { find(Item::class.java, itemId) }

        assertEquals(itemId, found.identifier)
        assertFalse(found.isNew)
    }

    @Test
    fun `an entity equals its proxy`() {
        runInTransaction { persist(Item(itemId)) }

        val found = runInTransaction { find(Item::class.java, itemId) }

        runInTransaction {
            val proxy = getReference(Item::class.java, itemId)

            assertNotSame(Item::class.java, proxy.javaClass)
            assertEquals(found, proxy)
            assertEquals(proxy, found)
        }
    }

    companion object {

        private val sequence = AtomicLong()
    }
}
