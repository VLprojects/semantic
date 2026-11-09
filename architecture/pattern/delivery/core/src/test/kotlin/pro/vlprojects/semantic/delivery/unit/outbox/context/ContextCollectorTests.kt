package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.AttributeProvider
import pro.vlprojects.semantic.delivery.outbox.context.ContextCollector
import pro.vlprojects.semantic.delivery.outbox.context.ContextProvider

class ContextCollectorTests {

    private val keyProvider = ContextProvider { _, builder -> builder.setOrderingKey("first") }
    private val laterKeyProvider = ContextProvider { _, builder -> builder.setOrderingKey("second") }
    private val attributeProvider = AttributeProvider { mapOf("trace" to "abc", "tenant" to "provided") }

    @Test
    fun `collecting gives the value of the last provider`() {
        val context = listOf(keyProvider, laterKeyProvider)
            .let(::ContextCollector)
            .collect("order")

        assertEquals("second", context.orderingKey)
    }

    @Test
    fun `collecting gives the attribute of the last provider`() {
        val laterAttributeProvider = AttributeProvider { mapOf("tenant" to "later") }

        val context = listOf(attributeProvider, laterAttributeProvider)
            .let(::ContextCollector)
            .collect("order")

        assertEquals(mapOf("trace" to "abc", "tenant" to "later"), context.attributes)
    }
}
