package pro.vlprojects.semantic.ddd.spring.data.mongodb.repository.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Database.template
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Item
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.ItemId
import java.util.concurrent.atomic.AtomicLong

class MongoEntityRepositoryTests {

    private val itemId = ItemId(sequence.incrementAndGet())

    private val items = MongoRepositoryFactory(template).getRepository(ItemRepository::class.java)

    @Test
    fun `an entity is found by its identifier`() {
        template.insert(Item(itemId))

        val found = items.findByIdOrThrow(itemId)

        assertEquals(itemId, found.identifier)
    }

    @Test
    fun `an entity that is not stored is not found`() {
        val error = assertThrows<NotFoundException> { items.findByIdOrThrow(itemId) }

        assertEquals("Entity not found [id: $itemId]", error.message)
    }

    companion object {

        // Above the keys the tests of the bases store in the same collection.
        private val sequence = AtomicLong(1_000_000)
    }
}
