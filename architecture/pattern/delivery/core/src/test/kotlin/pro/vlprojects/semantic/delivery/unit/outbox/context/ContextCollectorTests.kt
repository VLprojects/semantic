package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.AttributeProvider
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.context.ContextProvider

class ContextCollectorTests {

    private val targetProvider = ContextProvider { _, builder -> builder.setTarget("first") }
    private val laterTargetProvider = ContextProvider { _, builder -> builder.setTarget("second") }
    private val attributeProvider = AttributeProvider { mapOf("trace" to "abc", "tenant" to "provided") }

    @Test
    fun `collecting gives the value of the last provider`() {
        val context = listOf(targetProvider, laterTargetProvider)
            .let(::ContextCollector)
            .collect("order")

        assertEquals("second", context.target)
    }

    @Test
    fun `collecting gives the attribute of the last provider`() {
        val laterAttributeProvider = AttributeProvider { mapOf("tenant" to "later") }

        val context = listOf(targetProvider, attributeProvider, laterAttributeProvider)
            .let(::ContextCollector)
            .collect("order")

        assertEquals(mapOf("trace" to "abc", "tenant" to "later"), context.attributes)
    }

    @Test
    fun `collecting without a target fails`() {
        val collector = listOf(attributeProvider).let(::ContextCollector)

        assertThrows(IllegalStateException::class.java) { collector.collect("order") }
    }
}
