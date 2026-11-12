package pro.vlprojects.semantic.pattern.unit.enterprise.configuration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.enterprise.configuration.ConfigurationDefinition

class ConfigurationDefinitionTests {

    @Test
    fun `definitions of one name, type and arguments are equal`() {
        val definition = ConfigurationDefinition.of("orders", "fixed", mapOf("delay" to "1s"))
        val same = ConfigurationDefinition.of("orders", "fixed", mapOf("delay" to "1s"))

        assertEquals(same, definition)
    }

    @Test
    fun `definitions of other arguments differ`() {
        val definition = ConfigurationDefinition.of("orders", "fixed", mapOf("delay" to "1s"))
        val other = ConfigurationDefinition.of("orders", "fixed", mapOf("delay" to "2s"))

        assertNotEquals(other, definition)
    }

    @Test
    fun `a definition of an empty block names no type`() {
        val definition = ConfigurationDefinition.of("audit")

        assertNull(definition.typeName)
        assertEquals(emptyMap<String, String>(), definition.arguments)
    }

    @Test
    fun `a definition shows the keys of its arguments and hides their values`() {
        val definition = ConfigurationDefinition.of("orders", "basic", mapOf("password" to "secret"))

        assertEquals("[name: orders, typeName: basic, arguments: [password]]", definition.toString())
    }
}
