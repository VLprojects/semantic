package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.Context

class ContextTests {

    @Test
    fun `creating without keys generates different ones`() {
        val context = Context.of("rabbit")

        assertNotEquals(context.orderingKey, context.idempotencyKey)
    }

    @Test
    fun `creating keeps the given keys`() {
        val context = Context.of("rabbit", orderingKey = "order-1", idempotencyKey = "request-1")

        assertEquals("order-1", context.orderingKey)
        assertEquals("request-1", context.idempotencyKey)
    }
}
