package pro.vlprojects.semantic.pattern.unit.enterprise.configuration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.enterprise.configuration.Definition

class DefinitionTests {

    @Test
    fun `definitions of one name, type and arguments are equal`() {
        val definition = Definition.of("orders", "fixed", mapOf("delay" to "1s"))
        val same = Definition.of("orders", "fixed", mapOf("delay" to "1s"))

        assertEquals(same, definition)
        assertEquals(same.hashCode(), definition.hashCode())
    }

    @Test
    fun `definitions of other names differ`() {
        val definition = Definition.of("orders", "fixed")
        val other = Definition.of("payments", "fixed")

        assertNotEquals(other, definition)
    }

    @Test
    fun `definitions of other types differ`() {
        val definition = Definition.of("orders", "fixed")
        val other = Definition.of("orders", "exponential")

        assertNotEquals(other, definition)
    }

    @Test
    fun `definitions of other arguments differ`() {
        val definition = Definition.of("orders", "fixed", mapOf("delay" to "1s"))
        val other = Definition.of("orders", "fixed", mapOf("delay" to "2s"))

        assertNotEquals(other, definition)
    }

    @Test
    fun `a definition keeps its arguments when the given map changes`() {
        val arguments = mutableMapOf("delay" to "1s")
        val definition = Definition.of("orders", "fixed", arguments)

        arguments["delay"] = "2s"

        assertEquals(mapOf("delay" to "1s"), definition.arguments)
    }

    @Test
    fun `a definition of a blank name is rejected`() {
        assertThrows<IllegalArgumentException> { Definition.of(" ", "fixed") }
    }

    @Test
    fun `a definition of a blank type is rejected`() {
        assertThrows<IllegalArgumentException> { Definition.of("orders", "") }
    }

    @Test
    fun `a definition of an argument with a blank key is rejected`() {
        assertThrows<IllegalArgumentException> { Definition.of("orders", "fixed", mapOf("" to "1s")) }
    }

    @Test
    fun `a definition shows the keys of its arguments and hides their values`() {
        val definition = Definition.of("orders", "basic", mapOf("password" to "secret"))

        assertEquals("[name: orders, type: basic, arguments: [password]]", definition.toString())
    }
}
