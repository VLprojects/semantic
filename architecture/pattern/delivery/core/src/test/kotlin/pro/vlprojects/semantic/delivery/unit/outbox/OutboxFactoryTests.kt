package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.OutboxFactory
import pro.vlprojects.semantic.delivery.outbox.OutboxPayloadSerializer
import pro.vlprojects.semantic.delivery.outbox.OutboxStatus
import pro.vlprojects.semantic.delivery.outbox.annotation.IdempotencyKey
import pro.vlprojects.semantic.delivery.outbox.context.OutboxContextProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OutboxFactoryTests {

    private class Payment(@IdempotencyKey val operation: String)

    private interface Operation {

        @get:IdempotencyKey
        val operation: String
    }

    private class Refund(override val operation: String) : Operation

    private object TextSerializer : OutboxPayloadSerializer {

        override fun serialize(payload: Any) = "text"

        override fun <T : Any> deserialize(text: String, type: Class<T>) = error("not used")
    }

    private val moment = Instant.parse("2026-11-09T00:00:00Z")
    private val factory = OutboxFactory(
        serializer = TextSerializer,
        contextProviders = listOf(OutboxContextProvider { mapOf("trace" to "provided", "tenant" to "acme") }),
        clock = Clock.fixed(moment, ZoneOffset.UTC),
    )

    @Test
    fun `an outbox is new and due at once for its deliverer`() {
        val outbox = factory.create("payload", "order-1", "broker")

        assertEquals("order-1", outbox.key)
        assertEquals("broker", outbox.delivererId)
        assertEquals("java.lang.String", outbox.payloadType)
        assertEquals("text", outbox.payload)
        assertEquals(OutboxStatus.NEW, outbox.status)
        assertEquals(moment, outbox.createdAt)
        assertEquals(moment, outbox.nextRetryAt)
    }

    @Test
    fun `an explicit context wins over a provided one`() {
        val outbox = factory.create("payload", "order-1", "broker", mapOf("trace" to "explicit"))

        assertEquals(mapOf("trace" to "explicit", "tenant" to "acme"), outbox.context)
    }

    @Test
    fun `an explicit idempotency key wins over the annotated one`() {
        val outbox = factory.create(Payment("annotated"), "order-1", "broker", idempotencyKey = "explicit")

        assertEquals("explicit", outbox.idempotencyKey)
    }

    @Test
    fun `the idempotency key is read from an annotated field`() {
        assertEquals("annotated", factory.create(Payment("annotated"), "broker").idempotencyKey)
    }

    @Test
    fun `the idempotency key is read from a getter of an interface`() {
        assertEquals("annotated", factory.create(Refund("annotated"), "broker").idempotencyKey)
    }

    @Test
    fun `without a key both the key and the idempotency key are the id`() {
        val outbox = factory.create("payload", "broker")

        assertEquals(outbox.id, outbox.key)
        assertEquals(outbox.id, outbox.idempotencyKey)
    }
}
