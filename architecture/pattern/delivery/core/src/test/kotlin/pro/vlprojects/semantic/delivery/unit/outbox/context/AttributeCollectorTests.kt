package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.AttributeCollector
import pro.vlprojects.semantic.delivery.outbox.context.AttributeProvider

class AttributeCollectorTests {

    private val tenant = AttributeProvider { mapOf("tenant" to "first", "trace" to "abc") }
    private val laterTenant = AttributeProvider { mapOf("tenant" to "second") }
    private val failing = AttributeProvider { throw IllegalStateException("unavailable") }

    @Test
    fun `collecting merges the providers and the later one wins`() {
        val attributes = listOf(tenant, laterTenant)
            .let(::AttributeCollector)
            .collect()

        assertEquals(mapOf("tenant" to "second", "trace" to "abc"), attributes)
    }

    @Test
    fun `collecting skips a failed provider`() {
        val attributes = listOf(failing, tenant)
            .let(::AttributeCollector)
            .collect()

        assertEquals(mapOf("tenant" to "first", "trace" to "abc"), attributes)
    }

    @Test
    fun `collecting without providers gives no attributes`() {
        val attributes = AttributeCollector(emptyList()).collect()

        assertEquals(emptyMap<String, String>(), attributes)
    }
}
