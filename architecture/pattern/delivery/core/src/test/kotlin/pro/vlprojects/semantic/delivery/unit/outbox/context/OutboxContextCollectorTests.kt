package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.OutboxContextCollector
import pro.vlprojects.semantic.delivery.outbox.context.OutboxContextProvider

class OutboxContextCollectorTests {

    private val tenant = OutboxContextProvider { mapOf("tenant" to "first", "trace" to "abc") }
    private val laterTenant = OutboxContextProvider { mapOf("tenant" to "second") }
    private val failing = OutboxContextProvider { throw IllegalStateException("unavailable") }

    @Test
    fun `collecting merges the providers and the later one wins`() {
        val context = listOf(tenant, laterTenant)
            .let(::OutboxContextCollector)
            .collect()

        assertEquals(mapOf("tenant" to "second", "trace" to "abc"), context)
    }

    @Test
    fun `collecting skips a failed provider`() {
        val context = listOf(failing, tenant)
            .let(::OutboxContextCollector)
            .collect()

        assertEquals(mapOf("tenant" to "first", "trace" to "abc"), context)
    }

    @Test
    fun `collecting without providers gives an empty context`() {
        val context = OutboxContextCollector(emptyList()).collect()

        assertEquals(emptyMap<String, String>(), context)
    }
}
