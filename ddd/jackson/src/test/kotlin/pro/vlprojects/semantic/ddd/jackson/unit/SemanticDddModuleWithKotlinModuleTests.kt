package pro.vlprojects.semantic.ddd.jackson.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.jackson.SemanticDddModule
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue
import java.util.UUID

class SemanticDddModuleWithKotlinModuleTests {

    private val kotlinModule = KotlinModule.Builder().build()

    private val semanticDddModule = SemanticDddModule()

    private val mapper = JsonMapper.builder()
        .addModule(kotlinModule)
        .addModule(semanticDddModule)
        .build()

    private val uuidOfVersion7 = UUID.fromString("0192f0c1-7a3e-7c2d-9b1a-3f4e5d6c7b8a")

    private val orderId = OrderId(uuidOfVersion7)

    private val order = Order(orderId, "ann")

    private val orderJson = """{"id":"$uuidOfVersion7","customer":"ann"}"""

    @Test
    fun `writes an identifier inside a data class`() {
        val json = order.let(mapper::writeValueAsString)

        assertEquals(orderJson, json)
    }

    @Test
    fun `reads an identifier inside a data class`() {
        val orderFromJson = mapper.readValue<Order>(orderJson)

        assertEquals(order, orderFromJson)
    }

    @Test
    fun `reads an identifier through a factory it marks`() {
        val identifier = mapper.readValue<ParcelId>("\"$uuidOfVersion7\"")

        assertEquals(ParcelId.create(uuidOfVersion7), identifier)
    }

    @Test
    fun `reads a key of a map through a factory the identifier marks`() {
        val parcelId = ParcelId.create(uuidOfVersion7)
        val parcels = mapper.readValue<Map<ParcelId, String>>("{\"$uuidOfVersion7\":\"sent\"}")

        assertEquals(mapOf(parcelId to "sent"), parcels)
    }

    @Test
    fun `refuses an identifier whose constructor is not public`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<ShipmentId>("\"$uuidOfVersion7\"")
        }
    }

    @Test
    fun `refuses a key of an identifier whose constructor is not public`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<Map<ShipmentId, String>>("{\"$uuidOfVersion7\":\"sent\"}")
        }
    }

    @Test
    fun `refuses an identifier with no constructor of its value`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<CouponId>("\"4006381333931\"")
        }
    }
}
