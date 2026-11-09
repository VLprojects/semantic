package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.AttributeProvider
import pro.vlprojects.semantic.delivery.outbox.context.Context
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.context.ContextProvider

class ContextCollectorTests {

    private val targetProvider = ContextProvider { _, context -> context.withTarget("first") }
    private val laterTargetProvider = ContextProvider { _, context -> context.withTarget("second") }
    private val attributeProvider = AttributeProvider { mapOf("trace" to "abc", "tenant" to "provided") }

    @Test
    fun `collecting passes the result of a provider to the next one`() {
        val context = listOf(targetProvider, laterTargetProvider)
            .let(::ContextCollector)
            .collect("order")

        assertEquals("second", context.target)
    }

    @Test
    fun `collecting keeps the given context when providers add nothing`() {
        val context = emptyList<ContextProvider>()
            .let(::ContextCollector)
            .collect("order", Context.of("given"))

        assertEquals("given", context.target)
    }

    @Test
    fun `collecting prefers the given attributes to the provided ones`() {
        val given = Context.of("given", attributes = mapOf("tenant" to "given"))

        val context = listOf(attributeProvider)
            .let(::ContextCollector)
            .collect("order", given)

        assertEquals(mapOf("trace" to "abc", "tenant" to "given"), context.attributes)
    }

    @Test
    fun `collecting without a target fails`() {
        val collector = listOf(attributeProvider).let(::ContextCollector)

        assertThrows(IllegalStateException::class.java) { collector.collect("order") }
    }
}
