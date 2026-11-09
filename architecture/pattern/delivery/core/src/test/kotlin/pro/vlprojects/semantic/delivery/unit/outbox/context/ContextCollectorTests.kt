package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.AttributeProvider
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.context.ContextProvider

class ContextCollectorTests {

    private val targetProvider = ContextProvider { _, builder -> builder.target("first") }
    private val laterTargetProvider = ContextProvider { _, builder -> builder.target("second") }
    private val attributeProvider = AttributeProvider { mapOf("trace" to "abc", "tenant" to "provided") }

    @Test
    fun `collecting gives the value of the last provider`() {
        val context = listOf(targetProvider, laterTargetProvider)
            .let(::ContextCollector)
            .collect("order")

        assertEquals("second", context.target)
    }

    @Test
    fun `collecting prefers the explicit values`() {
        val context = listOf(targetProvider)
            .let(::ContextCollector)
            .collect("order") { target("explicit") }

        assertEquals("explicit", context.target)
    }

    @Test
    fun `collecting prefers the explicit attributes to the provided ones`() {
        val context = listOf(targetProvider, attributeProvider)
            .let(::ContextCollector)
            .collect("order") { attributes(mapOf("tenant" to "given")) }

        assertEquals(mapOf("trace" to "abc", "tenant" to "given"), context.attributes)
    }

    @Test
    fun `collecting without a target fails`() {
        val collector = listOf(attributeProvider).let(::ContextCollector)

        assertThrows(IllegalStateException::class.java) { collector.collect("order") }
    }
}
