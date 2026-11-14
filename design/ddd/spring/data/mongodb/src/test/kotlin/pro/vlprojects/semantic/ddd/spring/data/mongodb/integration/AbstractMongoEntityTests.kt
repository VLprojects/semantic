package pro.vlprojects.semantic.ddd.spring.data.mongodb.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Database.mappingContext
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Database.template
import java.util.concurrent.atomic.AtomicLong

class AbstractMongoEntityTests {

    private val itemId = ItemId(sequence.incrementAndGet())

    @Test
    fun `the identifier is the key and the version is the version`() {
        val entity = mappingContext.getRequiredPersistentEntity(Item::class.java)

        assertEquals("identifier", entity.requiredIdProperty.name)
        assertEquals("version", entity.requiredVersionProperty.name)
    }

    @Test
    fun `the identifier and the version are the only stored fields of the base`() {
        val fields = mappingContext
            .getRequiredPersistentEntity(Item::class.java)
            .map { it.fieldName }
            .toSet()

        assertEquals(setOf("_id", "version"), fields)
    }

    @Test
    fun `an entity is new until it is saved`() {
        val item = Item(itemId)

        assertTrue(item.isNew)
        template.save(item)
        assertFalse(item.isNew)
    }

    @Test
    fun `a loaded entity is not new`() {
        template.save(Item(itemId))

        val found = template.findById<Item>(itemId)

        assertEquals(itemId, found?.identifier)
        assertEquals(false, found?.isNew)
    }

    @Test
    fun `a saved entity is saved again without a second insert`() {
        val item = Item(itemId)

        template.save(item)
        template.save(item)

        val stored = Criteria
            .where("_id")
            .`is`(itemId)
            .let(::Query)
            .let { template.count(it, Item::class.java) }

        assertEquals(1, stored)
    }

    companion object {

        private val sequence = AtomicLong()
    }
}
