package pro.vlprojects.semantic.delivery.unit.observability

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.delivery.observability.InstrumentedOutboxRepository
import pro.vlprojects.semantic.delivery.observability.OutboxInstrumentation
import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxMetadata
import pro.vlprojects.semantic.delivery.outbox.OutboxRepository
import java.lang.reflect.Proxy
import java.time.Instant

class InstrumentedOutboxRepositoryTests {

    private val moment = Instant.parse("2026-11-09T00:00:00Z")
    private val outbox = Outbox("id", "key", "broker", "String", "{}", "operation", moment, moment)
    private val scheduled = mutableListOf<OutboxMetadata>()
    private val instrumentation = object : OutboxInstrumentation {

        override fun onScheduled(record: OutboxMetadata) {
            scheduled += record
        }
    }

    private fun createRepository(save: (Outbox) -> Unit) = Proxy.newProxyInstance(
        OutboxRepository::class.java.classLoader,
        arrayOf(OutboxRepository::class.java),
    ) { _, _, arguments -> save(arguments[0] as Outbox) } as OutboxRepository

    @Test
    fun `a saved outbox is reported as scheduled`() {
        val saved = mutableListOf<Outbox>()

        InstrumentedOutboxRepository(createRepository { saved += it }, instrumentation).save(outbox)

        assertSame(outbox, saved.single())
        assertEquals("id", scheduled.single().id)
    }

    @Test
    fun `an outbox that failed to save is not reported`() {
        val repository = InstrumentedOutboxRepository(createRepository { error("no transaction") }, instrumentation)

        assertThrows<IllegalStateException> { repository.save(outbox) }
        assertTrue(scheduled.isEmpty())
    }
}
