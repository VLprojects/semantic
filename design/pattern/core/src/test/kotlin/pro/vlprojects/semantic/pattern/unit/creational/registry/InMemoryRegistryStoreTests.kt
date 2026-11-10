package pro.vlprojects.semantic.pattern.unit.creational.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.creational.registry.InMemoryRegistryStore

class InMemoryRegistryStoreTests {

    private val store = InMemoryRegistryStore<String>()

    @Test
    fun `an absent name takes an instance`() {
        assertTrue(store.putIfAbsent("morning", "Good morning"))
        assertEquals("Good morning", store.find("morning"))
    }

    @Test
    fun `a taken name keeps its instance`() {
        store.putIfAbsent("morning", "Good morning")

        assertFalse(store.putIfAbsent("morning", "Morning"))
        assertEquals("Good morning", store.find("morning"))
    }

    @Test
    fun `a replace returns the instance it took out`() {
        store.putIfAbsent("morning", "Good morning")

        assertEquals("Good morning", store.getAndReplace("morning", "Morning"))
        assertEquals("Morning", store.find("morning"))
    }

    @Test
    fun `an absent name is not replaced`() {
        assertNull(store.getAndReplace("evening", "Good evening"))
        assertNull(store.find("evening"))
    }

    @Test
    fun `a remove returns the instance it took out`() {
        store.putIfAbsent("morning", "Good morning")

        assertEquals("Good morning", store.getAndRemove("morning"))
        assertNull(store.find("morning"))
        assertNull(store.getAndRemove("morning"))
    }

    @Test
    fun `a remove of all returns every instance and leaves the store empty`() {
        store.putIfAbsent("morning", "Good morning")
        store.putIfAbsent("evening", "Good evening")

        assertEquals(setOf("Good morning", "Good evening"), store.getAndRemoveAll().toSet())
        assertTrue(store.getAll().isEmpty())
    }

    @Test
    fun `all the instances are a snapshot that later changes do not touch`() {
        store.putIfAbsent("morning", "Good morning")
        val instances = store.getAll()

        store.putIfAbsent("evening", "Good evening")

        assertEquals(listOf("Good morning"), instances)
    }
}
