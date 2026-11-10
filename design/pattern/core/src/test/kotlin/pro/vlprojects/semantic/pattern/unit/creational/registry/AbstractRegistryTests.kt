package pro.vlprojects.semantic.pattern.unit.creational.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.creational.registry.Registry
import pro.vlprojects.semantic.pattern.creational.registry.error.DefaultInstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

class AbstractRegistryTests {

    private val registry = GreetingRegistry()

    @Test
    fun `a registered instance is found by its name`() {
        registry.register("morning", "Good morning")

        assertEquals("Good morning", registry["morning"])
        assertEquals("Good morning", registry.find("morning"))
        assertTrue("morning" in registry)
    }

    @Test
    fun `an unknown name finds nothing`() {
        assertNull(registry.find("evening"))
        assertFalse("evening" in registry)
        assertEquals("evening", assertThrows<InstanceNotFoundException> { registry["evening"] }.name)
    }

    @Test
    fun `a blank name is not registered`() {
        assertThrows<InvalidNameException> { registry.register(" ", "Hello") }
        assertFalse(" " in registry)
    }

    @Test
    fun `a taken name keeps its first instance`() {
        registry.register("morning", "Good morning")

        assertThrows<InstanceAlreadyExistsException> { registry.register("morning", "Morning") }
        assertEquals("Good morning", registry["morning"])
    }

    @Test
    fun `a replaced instance is returned to its owner`() {
        registry.register("morning", "Good morning")

        assertEquals("Good morning", registry.replace("morning", "Morning"))
        assertEquals("Morning", registry["morning"])
    }

    @Test
    fun `an unregistered instance is returned to its owner`() {
        registry.register("morning", "Good morning")

        assertEquals("Good morning", registry.unregister("morning"))
        assertFalse("morning" in registry)
    }

    @Test
    fun `an unknown name is neither replaced nor unregistered`() {
        assertThrows<InstanceNotFoundException> { registry.replace("evening", "Good evening") }
        assertThrows<InstanceNotFoundException> { registry.unregister("evening") }
        assertFalse("evening" in registry)
    }

    @Test
    fun `the default instance is the one under the default name`() {
        registry.register(Registry.DEFAULT_NAME, "Hello")

        assertEquals("Hello", registry.default)
    }

    @Test
    fun `a registry without the default instance says so`() {
        assertThrows<DefaultInstanceNotFoundException> { registry.default }
        assertThrows<DefaultInstanceNotFoundException> { registry.getOrDefault("evening") }
    }

    @Test
    fun `an unknown name falls back to the default instance only when asked`() {
        registry.register(Registry.DEFAULT_NAME, "Hello")
        registry.register("morning", "Good morning")

        assertEquals("Good morning", registry.getOrDefault("morning"))
        assertEquals("Hello", registry.getOrDefault("evening"))
        assertThrows<InstanceNotFoundException> { registry["evening"] }
    }

    @Test
    fun `the instances are listed with their names`() {
        registry.register("morning", "Good morning")
        registry.register("evening", "Good evening")

        assertEquals(mapOf("morning" to "Good morning", "evening" to "Good evening"), registry.instances)
    }
}
