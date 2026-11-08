package pro.vlprojects.semantic.delivery.unit.outbox.routing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.OutboxRecordMetadata
import pro.vlprojects.semantic.delivery.outbox.routing.OutboxPayloadSelector
import pro.vlprojects.semantic.delivery.outbox.routing.OutboxPayloadTypeSelector
import pro.vlprojects.semantic.delivery.outbox.routing.OutboxRoute
import pro.vlprojects.semantic.delivery.outbox.routing.OutboxRouting
import java.time.Instant

class OutboxRoutingTests {

    private val metadata = OutboxRecordMetadata(
        id = "record",
        key = "order-1",
        delivererId = "deliverer",
        payloadType = "String",
        idempotencyKey = "operation",
        failureCount = 0,
        createdAt = Instant.parse("2026-11-09T00:00:00Z"),
        context = mapOf("region" to "eu"),
    )

    private val stringRoute = OutboxRoute("strings", OutboxPayloadTypeSelector(String::class.java))
    private val charSequenceRoute = OutboxRoute("sequences", OutboxPayloadTypeSelector(CharSequence::class.java))

    @Test
    fun `the first matching route wins`() {
        val route = OutboxRouting(listOf(stringRoute, charSequenceRoute)).findRoute("payload", metadata)

        assertEquals("strings", route?.target)
    }

    @Test
    fun `a selector matches a subtype of its type`() {
        val route = OutboxRouting(listOf(charSequenceRoute)).findRoute("payload", metadata)

        assertEquals("sequences", route?.target)
    }

    @Test
    fun `no route is found for a payload nothing selects`() {
        assertNull(OutboxRouting(listOf(stringRoute)).findRoute(1, metadata))
    }

    @Test
    fun `a selector reads the metadata`() {
        val selector = OutboxPayloadSelector { _, candidate -> candidate.context["region"] == "eu" }
        val route = OutboxRouting(listOf(OutboxRoute("europe", selector))).findRoute(1, metadata)

        assertEquals("europe", route?.target)
    }
}
