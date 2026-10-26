package pro.vlprojects.semantic.ddd.jackson.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.jackson.SemanticDddModule
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.readValue
import java.util.UUID

class SemanticDddModuleTests {

    private val semanticDddModule = SemanticDddModule()

    private val mapper = JsonMapper.builder()
        .addModule(semanticDddModule)
        .build()

    private val uuidOfVersion7 = UUID.fromString("0192f0c1-7a3e-7c2d-9b1a-3f4e5d6c7b8a")

    private val uuidOfVersion4 = UUID.fromString("6f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f")

    private val orderId = OrderId(uuidOfVersion7)

    private val invoiceId = InvoiceId(uuidOfVersion7)

    @Test
    fun `writes an identifier of a number as the number`() {
        val json = CartId(42).let(mapper::writeValueAsString)

        assertEquals("42", json)
    }

    @Test
    fun `writes an identifier of a UUID as the string of it`() {
        val json = orderId.let(mapper::writeValueAsString)

        assertEquals("\"$uuidOfVersion7\"", json)
    }

    @Test
    fun `reads an identifier of a Short from the number`() {
        val identifier = mapper.readValue<ShelfId>("7")

        assertEquals(ShelfId(7), identifier)
    }

    @Test
    fun `reads an identifier of a UUID from the string of it`() {
        val identifier = mapper.readValue<OrderId>("\"$uuidOfVersion7\"")

        assertEquals(orderId, identifier)
    }

    @Test
    fun `writes an identifier that implements the interface itself`() {
        val json = invoiceId.let(mapper::writeValueAsString)

        assertEquals("\"$uuidOfVersion7\"", json)
    }

    @Test
    fun `reads an identifier that implements the interface itself`() {
        val identifier = mapper.readValue<InvoiceId>("\"$uuidOfVersion7\"")

        assertEquals(uuidOfVersion7, identifier.value)
    }

    @Test
    fun `writes an identifier the way it marks itself`() {
        val json = TicketId(uuidOfVersion7).let(mapper::writeValueAsString)

        assertEquals("\"TCK-$uuidOfVersion7\"", json)
    }

    @Test
    fun `writes identifiers inside a collection`() {
        val json = listOf(orderId).let(mapper::writeValueAsString)

        assertEquals("[\"$uuidOfVersion7\"]", json)
    }

    @Test
    fun `reads identifiers inside a collection`() {
        val identifiers = mapper.readValue<List<OrderId>>("[\"$uuidOfVersion7\"]")

        assertEquals(listOf(orderId), identifiers)
    }

    @Test
    fun `writes an identifier as a value of a map`() {
        val json = mapOf("order" to orderId).let(mapper::writeValueAsString)

        assertEquals("{\"order\":\"$uuidOfVersion7\"}", json)
    }

    @Test
    fun `reads an identifier as a value of a map`() {
        val orderIds = mapper.readValue<Map<String, OrderId>>("{\"order\":\"$uuidOfVersion7\"}")

        assertEquals(mapOf("order" to orderId), orderIds)
    }

    @Test
    fun `writes an identifier as a key of a map`() {
        val json = mapOf(invoiceId to "paid").let(mapper::writeValueAsString)

        assertEquals("{\"$uuidOfVersion7\":\"paid\"}", json)
    }

    @Test
    fun `reads an identifier of a String as a key of a map`() {
        val stock = mapper.readValue<Map<SkuId, Int>>("{\"A-42\":3}")

        assertEquals(mapOf(SkuId("A-42") to 3), stock)
    }

    @Test
    fun `reads an identifier of a UUID as a key of a map`() {
        val statuses = mapper.readValue<Map<OrderId, String>>("{\"$uuidOfVersion7\":\"paid\"}")

        assertEquals(mapOf(orderId to "paid"), statuses)
    }

    @Test
    fun `reads an identifier of a number as a key of a map`() {
        val totals = mapper.readValue<Map<CartId, Int>>("{\"42\":3}")

        assertEquals(mapOf(CartId(42) to 3), totals)
    }

    @Test
    fun `reads an identifier of a Short as a key of a map`() {
        val shelves = mapper.readValue<Map<ShelfId, String>>("{\"7\":\"tea\"}")

        assertEquals(mapOf(ShelfId(7) to "tea"), shelves)
    }

    @Test
    fun `reads a key of an identifier with a deserializer of its own`() {
        val skuIdDeserializer = SkuIdDeserializer()
        val skuIdModule = SimpleModule().addDeserializer(SkuId::class.java, skuIdDeserializer)
        val mapperWithSkuIdModule = JsonMapper.builder()
            .addModule(skuIdModule)
            .addModule(semanticDddModule)
            .build()
        val stock = mapperWithSkuIdModule.readValue<Map<SkuId, Int>>("{\"A-42\":3}")

        assertEquals(mapOf(SkuId("A-42") to 3), stock)
    }

    @Test
    fun `refuses a key the identifier rejects`() {
        assertThrows<ValueInstantiationException> {
            mapper.readValue<Map<TrackingId, String>>("{\"$uuidOfVersion4\":\"sent\"}")
        }
    }

    @Test
    fun `leaves alone a constructor the identifier disabled for a key`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<Map<RefundId, String>>("{\"$uuidOfVersion7\":\"paid\"}")
        }
    }

    @Test
    fun `refuses a value the identifier rejects`() {
        assertThrows<ValueInstantiationException> {
            mapper.readValue<TrackingId>("\"$uuidOfVersion4\"")
        }
    }

    @Test
    fun `leaves alone a constructor the identifier disabled`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<RefundId>("\"$uuidOfVersion7\"")
        }
    }

    @Test
    fun `is found among the modules on the classpath`() {
        val mapperWithFoundModules = JsonMapper.builder()
            .findAndAddModules()
            .build()
        val json = CartId(42).let(mapperWithFoundModules::writeValueAsString)

        assertEquals("42", json)
    }
}
