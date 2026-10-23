package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractIdentifier
import java.util.UUID

class AbstractIdentifierTests {

    private class OrderId(
        value: UUID,
    ) : AbstractIdentifier<UUID>(value)

    private class CustomerId(
        value: UUID,
    ) : AbstractIdentifier<UUID>(value)

    private val uuid = UUID.fromString("0192f0c4-0000-7000-8000-000000000001")

    @Test
    fun `identifiers of one class with one value are equal`() {
        assertEquals(OrderId(uuid), OrderId(uuid))
        assertEquals(OrderId(uuid).hashCode(), OrderId(uuid).hashCode())
    }

    @Test
    fun `identifiers of different classes differ even with one value`() {
        val order: Identifier = OrderId(uuid)
        val customer: Identifier = CustomerId(uuid)

        assertNotEquals(order, customer)
    }

    @Test
    fun `an identifier shows its value`() {
        assertEquals(uuid.toString(), OrderId(uuid).toString())
    }
}
