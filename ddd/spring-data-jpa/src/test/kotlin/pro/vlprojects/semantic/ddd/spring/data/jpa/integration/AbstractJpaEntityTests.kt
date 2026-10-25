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
    fun `the identifier is the only stored field of the base`() {
        val attributes = runInTransaction {
            this.metamodel
                .entity(Item::class.java)
                .attributes
                .map { it.name }
                .toSet()
        }

        assertEquals(setOf("identifier"), attributes)
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
            val target = org.hibernate.Hibernate.unproxy(proxy) as Item
            val initializer = org.hibernate.proxy.HibernateProxy.extractLazyInitializer(proxy)
            assertEquals(
                found,
                proxy,
                "proxy=${proxy.javaClass} interfaces=${proxy.javaClass.interfaces.toList()} initializer=$initializer " +
                    "target=${target.javaClass} sameAsProxy=${target === proxy} " +
                    "foundId=${found.identifier.javaClass}/${found.identifier} targetId=${target.identifier.javaClass}/${target.identifier} " +
                    "idsEqual=${found.identifier == target.identifier} foundUser=${org.springframework.data.util.ProxyUtils.getUserClass(found)} " +
                    "targetUser=${org.springframework.data.util.ProxyUtils.getUserClass(target)}",
            )
            assertEquals(proxy, found)
        }
    }

    companion object {

        private val sequence = AtomicLong()
    }
}
