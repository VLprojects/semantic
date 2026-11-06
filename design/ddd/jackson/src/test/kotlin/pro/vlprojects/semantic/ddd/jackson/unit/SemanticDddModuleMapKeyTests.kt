package pro.vlprojects.semantic.ddd.jackson.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.jackson.SemanticDddModule
import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.util.UUID

class SemanticDddModuleMapKeyTests {

    private val semanticDddModule = SemanticDddModule()

    private val mapper = JsonMapper.builder()
        .addModule(semanticDddModule)
        .build()

    private val uuidOfVersion7 = UUID.fromString("0192f0c1-7a3e-7c2d-9b1a-3f4e5d6c7b8a")

    private val uuidOfVersion4 = UUID.fromString("6f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f")

    private val orderId = OrderId(uuidOfVersion7)

    private val invoiceId = InvoiceId(uuidOfVersion7)

    private val cartId = CartId(42)

    private val cartIdKeyDeserializer = CartIdKeyDeserializer()

    private val cartIdModule = SimpleModule().addKeyDeserializer(CartId::class.java, cartIdKeyDeserializer)

    private val labelIdDeserializer = LabelIdDeserializer()

    private val labelIdModule = SimpleModule().addDeserializer(LabelId::class.java, labelIdDeserializer)

    private val voucherIdDeserializer = VoucherIdDeserializer()

    private val voucherIdModule = SimpleModule().addDeserializer(VoucherId::class.java, voucherIdDeserializer)

    private val prefixedUuidKeyDeserializer = PrefixedUuidKeyDeserializer()

    private val prefixedUuidModule = SimpleModule().addKeyDeserializer(UUID::class.java, prefixedUuidKeyDeserializer)

    private val forwardingKeyDeserializerModifier = ForwardingKeyDeserializerModifier()

    private val forwardingModule = SimpleModule().setDeserializerModifier(forwardingKeyDeserializerModifier)

    @Test
    fun `writes an identifier as a key of a map`() {
        val json = mapOf(invoiceId to "paid").let(mapper::writeValueAsString)

        assertEquals("{\"$uuidOfVersion7\":\"paid\"}", json)
    }

    @Test
    fun `reads an identifier of a String as a key of a map`() {
        val skuId = SkuId("A-42")
        val stock = mapper.readValue<Map<SkuId, Int>>("{\"A-42\":3}")

        val expectedStock = mapOf(skuId to 3)
        assertEquals(expectedStock, stock)
    }

    @Test
    fun `reads an identifier of a UUID as a key of a map`() {
        val statuses = mapper.readValue<Map<OrderId, String>>("{\"$uuidOfVersion7\":\"paid\"}")

        val expectedStatuses = mapOf(orderId to "paid")
        assertEquals(expectedStatuses, statuses)
    }

    @Test
    fun `reads an identifier of a Long as a key of a map`() {
        val totals = mapper.readValue<Map<CartId, Int>>("{\"42\":3}")

        val expectedTotals = mapOf(cartId to 3)
        assertEquals(expectedTotals, totals)
    }

    @Test
    fun `reads an identifier of an Int as a key of a map`() {
        val seatId = SeatId(12)
        val guests = mapper.readValue<Map<SeatId, String>>("{\"12\":\"ann\"}")

        val expectedGuests = mapOf(seatId to "ann")
        assertEquals(expectedGuests, guests)
    }

    @Test
    fun `reads an identifier of a Short as a key of a map`() {
        val shelfId = ShelfId(7)
        val shelves = mapper.readValue<Map<ShelfId, String>>("{\"7\":\"tea\"}")

        val expectedShelves = mapOf(shelfId to "tea")
        assertEquals(expectedShelves, shelves)
    }

    @Test
    fun `reads an identifier of a Double as a key of a map`() {
        val scoreId = ScoreId(1.5)
        val ranks = mapper.readValue<Map<ScoreId, Int>>("{\"1.5\":2}")

        val expectedRanks = mapOf(scoreId to 2)
        assertEquals(expectedRanks, ranks)
    }

    @Test
    fun `reads an identifier of a Boolean as a key of a map`() {
        val flagId = FlagId(true)
        val counts = mapper.readValue<Map<FlagId, Int>>("{\"true\":4}")

        val expectedCounts = mapOf(flagId to 4)
        assertEquals(expectedCounts, counts)
    }

    @Test
    fun `reads an identifier of a BigDecimal as a key of a map`() {
        val weightId = WeightId(BigDecimal("1.25"))
        val parcels = mapper.readValue<Map<WeightId, Int>>("{\"1.25\":2}")

        val expectedParcels = mapOf(weightId to 2)
        assertEquals(expectedParcels, parcels)
    }

    @Test
    fun `reads an identifier of a Char as a key of a map`() {
        val gradeId = GradeId('A')
        val pupils = mapper.readValue<Map<GradeId, Int>>("{\"A\":30}")

        val expectedPupils = mapOf(gradeId to 30)
        assertEquals(expectedPupils, pupils)
    }

    @Test
    fun `reads an identifier as a key of a map through the key deserializer of its value`() {
        val mapperWithPrefixedUuidModule = JsonMapper.builder()
            .addModule(semanticDddModule)
            .addModule(prefixedUuidModule)
            .build()
        val statuses = mapperWithPrefixedUuidModule.readValue<Map<OrderId, String>>("{\"U-$uuidOfVersion7\":\"paid\"}")

        val expectedStatuses = mapOf(orderId to "paid")
        assertEquals(expectedStatuses, statuses)
    }

    @Test
    fun `reads an identifier of an enum as a key of a map`() {
        val region = Region.EUROPE
        val stores = mapper.readValue<Map<Region, Int>>("{\"eu\":5}")

        val expectedStores = mapOf(region to 5)
        assertEquals(expectedStores, stores)
    }

    @Test
    fun `reads an identifier with a deserializer of its own as a key of a map through its fromString`() {
        val labelId = LabelId(uuidOfVersion7)
        val mapperWithLabelIdModule = JsonMapper.builder()
            .addModule(labelIdModule)
            .addModule(semanticDddModule)
            .build()
        val labels = mapperWithLabelIdModule.readValue<Map<LabelId, Int>>("{\"LBL-$uuidOfVersion7\":3}")

        val expectedLabels = mapOf(labelId to 3)
        assertEquals(expectedLabels, labels)
    }

    @Test
    fun `reads an identifier through a factory of a String before its constructor as a key of a map`() {
        val passId = PassId(uuidOfVersion7)
        val passes = mapper.readValue<Map<PassId, String>>("{\"PASS-$uuidOfVersion7\":\"ann\"}")

        val expectedPasses = mapOf(passId to "ann")
        assertEquals(expectedPasses, passes)
    }

    @Test
    fun `reads an identifier as a key of a map with the modules found on the classpath`() {
        val mapperWithFoundModules = JsonMapper.builder()
            .findAndAddModules()
            .build()
        val totals = mapperWithFoundModules.readValue<Map<CartId, Int>>("{\"42\":3}")

        val expectedTotals = mapOf(cartId to 3)
        assertEquals(expectedTotals, totals)
    }

    @Test
    fun `reads an identifier with a key deserializer it marks as a key of a map`() {
        val dockId = DockId(7)
        val docks = mapper.readValue<Map<DockId, Int>>("{\"D-7\":2}")

        val expectedDocks = mapOf(dockId to 2)
        assertEquals(expectedDocks, docks)
    }

    @Test
    fun `reads an identifier as a key of a map through a module registered after this one`() {
        val mapperWithCartIdModule = JsonMapper.builder()
            .addModule(semanticDddModule)
            .addModule(cartIdModule)
            .build()
        val totals = mapperWithCartIdModule.readValue<Map<CartId, Int>>("{\"C-42\":3}")

        val expectedTotals = mapOf(cartId to 3)
        assertEquals(expectedTotals, totals)
    }

    @Test
    fun `reads an identifier as a key of a map through this module registered after another one`() {
        val mapperWithCartIdModule = JsonMapper.builder()
            .addModule(cartIdModule)
            .addModule(semanticDddModule)
            .build()
        val totals = mapperWithCartIdModule.readValue<Map<CartId, Int>>("{\"42\":3}")

        val expectedTotals = mapOf(cartId to 3)
        assertEquals(expectedTotals, totals)
    }

    @Test
    fun `reads an identifier as a key of a map behind a wrapper that does not resolve it`() {
        val mapperWithForwardingModule = JsonMapper.builder()
            .addModule(semanticDddModule)
            .addModule(forwardingModule)
            .build()
        val statuses = mapperWithForwardingModule.readValue<Map<OrderId, String>>("{\"$uuidOfVersion7\":\"paid\"}")

        val expectedStatuses = mapOf(orderId to "paid")
        assertEquals(expectedStatuses, statuses)
    }

    @Test
    fun `refuses a key the identifier rejects`() {
        assertThrows<ValueInstantiationException> {
            mapper.readValue<Map<TrackingId, String>>("{\"$uuidOfVersion4\":\"sent\"}")
        }
    }

    @Test
    fun `refuses a key of a String the identifier rejects`() {
        assertThrows<ValueInstantiationException> {
            mapper.readValue<Map<BadgeId, String>>("{\"X-1\":\"ann\"}")
        }
    }

    @Test
    fun `names the identifier in a key that is not its value`() {
        val exception = assertThrows<InvalidFormatException> {
            mapper.readValue<Map<OrderId, String>>("{\"order\":\"paid\"}")
        }

        assertEquals(OrderId::class.java, exception.targetType)
    }

    @Test
    fun `tells what the value of a key that is not its value should be`() {
        val exception = assertThrows<InvalidFormatException> {
            mapper.readValue<Map<OrderId, String>>("{\"order\":\"paid\"}")
        }

        exception
            .originalMessage
            .endsWith("not a valid UUID")
            .let(::assertTrue)
    }

    @Test
    fun `names the identifier in a key that is not its number`() {
        val exception = assertThrows<InvalidFormatException> {
            mapper.readValue<Map<CartId, Int>>("{\"abc\":3}")
        }

        assertEquals(CartId::class.java, exception.targetType)
    }

    @Test
    fun `names the identifier in a key beyond the range of its value`() {
        val exception = assertThrows<InvalidFormatException> {
            mapper.readValue<Map<ShelfId, String>>("{\"70000\":\"tea\"}")
        }

        assertEquals(ShelfId::class.java, exception.targetType)
    }

    @Test
    fun `keeps the failure of the value as the cause of a key that is not its value`() {
        val exception = assertThrows<InvalidFormatException> {
            mapper.readValue<Map<OrderId, String>>("{\"order\":\"paid\"}")
        }

        assertInstanceOf(InvalidFormatException::class.java, exception.cause)
    }

    @Test
    fun `asks a problem handler once and about the value of a key that is not its value`() {
        val keyTypeRecordingProblemHandler = KeyTypeRecordingProblemHandler()
        val mapperWithProblemHandler = JsonMapper.builder()
            .addModule(semanticDddModule)
            .addHandler(keyTypeRecordingProblemHandler)
            .build()
        val expectedKeyTypes = listOf(Short::class.javaObjectType)

        assertThrows<InvalidFormatException> {
            mapperWithProblemHandler.readValue<Map<ShelfId, String>>("{\"70000\":\"tea\"}")
        }
        assertEquals(expectedKeyTypes, keyTypeRecordingProblemHandler.weirdKeyTypes)
    }

    @Test
    fun `reads an identifier from the value a problem handler gives for a key`() {
        val lowestShelfProblemHandler = LowestShelfProblemHandler()
        val mapperWithProblemHandler = JsonMapper.builder()
            .addModule(semanticDddModule)
            .addHandler(lowestShelfProblemHandler)
            .build()
        val shelfId = ShelfId(LowestShelfProblemHandler.LOWEST_SHELF)
        val shelves = mapperWithProblemHandler.readValue<Map<ShelfId, String>>("{\"70000\":\"tea\"}")

        val expectedShelves = mapOf(shelfId to "tea")
        assertEquals(expectedShelves, shelves)
    }

    @Test
    fun `refuses an identifier of no concrete class as a key of a map`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<Map<BoxedIdentifier<UUID>, String>>("{\"$uuidOfVersion7\":\"paid\"}")
        }
    }

    @Test
    fun `refuses an identifier with two factories of a String as a key of a map`() {
        val mapperWithVoucherIdModule = JsonMapper.builder()
            .addModule(voucherIdModule)
            .addModule(semanticDddModule)
            .build()

        assertThrows<InvalidDefinitionException> {
            mapperWithVoucherIdModule.readValue<Map<VoucherId, String>>("{\"V-$uuidOfVersion7\":\"paid\"}")
        }
    }

    @Test
    fun `leaves alone a constructor the identifier disabled for a key`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<Map<RefundId, String>>("{\"$uuidOfVersion7\":\"paid\"}")
        }
    }

    @Test
    fun `refuses an identifier with no creator as a key of a map behind a wrapper that does not resolve it`() {
        val mapperWithForwardingModule = JsonMapper.builder()
            .addModule(semanticDddModule)
            .addModule(forwardingModule)
            .build()

        assertThrows<InvalidDefinitionException> {
            mapperWithForwardingModule.readValue<Map<RefundId, String>>("{\"$uuidOfVersion7\":\"paid\"}")
        }
    }

    @Test
    fun `refuses an identifier whose value Jackson does not read as a key`() {
        assertThrows<InvalidDefinitionException> {
            mapper.readValue<Map<BerthId, String>>("{\"1:2\":\"tea\"}")
        }
    }

    @Test
    fun `refuses an identifier with two factories of a String as a key behind a wrapper that does not resolve it`() {
        val mapperWithVoucherIdModule = JsonMapper.builder()
            .addModule(voucherIdModule)
            .addModule(semanticDddModule)
            .addModule(forwardingModule)
            .build()

        assertThrows<InvalidDefinitionException> {
            mapperWithVoucherIdModule.readValue<Map<VoucherId, String>>("{\"V-$uuidOfVersion7\":\"paid\"}")
        }
    }
}
