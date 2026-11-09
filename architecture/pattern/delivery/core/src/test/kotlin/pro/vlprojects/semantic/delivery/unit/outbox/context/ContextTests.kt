package pro.vlprojects.semantic.delivery.unit.outbox.context

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.context.Context

class ContextTests {

    @Test
    fun `creating without keys leaves them empty`() {
        val context = Context.of("rabbit")

        assertNull(context.orderingKey)
        assertNull(context.idempotencyKey)
    }

    @Test
    fun `creating keeps the given keys`() {
        val context = Context.of("rabbit", orderingKey = "order-1", idempotencyKey = "request-1")

        assertEquals("order-1", context.orderingKey)
        assertEquals("request-1", context.idempotencyKey)
    }
}
