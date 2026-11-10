package pro.vlprojects.semantic.pattern.unit.creational.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.creational.registry.InMemoryRegistryStore

class InMemoryRegistryStoreTests {

    private val store = InMemoryRegistryStore<String>()

    @Test
    fun `an absent name takes an instance`() {
        assertNull(store.putIfAbsent("morning", "Good morning"))
        assertEquals("Good morning", store.find("morning"))
    }

    @Test
    fun `a taken name keeps its instance and returns it`() {
        store.putIfAbsent("morning", "Good morning")

        assertEquals("Good morning", store.putIfAbsent("morning", "Morning"))
        assertEquals("Good morning", store.find("morning"))
    }

    @Test
    fun `a replace returns the instance it took out`() {
        store.putIfAbsent("morning", "Good morning")

        assertEquals("Good morning", store.replace("morning", "Morning"))
        assertEquals("Morning", store.find("morning"))
    }

    @Test
    fun `an absent name is not replaced`() {
        assertNull(store.replace("evening", "Good evening"))
        assertNull(store.find("evening"))
    }

    @Test
    fun `a remove returns the instance it took out`() {
        store.putIfAbsent("morning", "Good morning")

        assertEquals("Good morning", store.remove("morning"))
        assertNull(store.find("morning"))
        assertNull(store.remove("morning"))
    }
}
