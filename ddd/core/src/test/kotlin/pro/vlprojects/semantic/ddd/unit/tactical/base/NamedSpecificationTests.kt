package pro.vlprojects.semantic.ddd.unit.tactical.base

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NamedSpecificationTests {

    @Test
    fun `a condition checks the property`() {
        assertTrue(IsOpen.isSatisfiedBy(order(status = OrderStatus.OPEN)))
        assertFalse(IsOpen.isSatisfiedBy(order(status = OrderStatus.PAID)))
    }

    @Test
    fun `a comparison takes the bound in`() {
        assertTrue(TotalAtLeast(100).isSatisfiedBy(order(total = 100)))
        assertFalse(TotalAtLeast(100).isSatisfiedBy(order(total = 99)))
    }

    @Test
    fun `a nested path reads through the properties`() {
        assertTrue(OfCustomer("ann").isSatisfiedBy(order(customer = Customer("ann", null))))
        assertFalse(OfCustomer("ann").isSatisfiedBy(order(customer = Customer("bob", null))))
    }

    @Test
    fun `a nested path through null gives null`() {
        assertFalse(OfCustomer("ann").isSatisfiedBy(order(customer = null)))
        assertTrue(WithoutEmail().isSatisfiedBy(order(customer = null)))
    }

    @Test
    fun `any checks the elements of a collection`() {
        val lines = listOf(OrderLine("a", 1), OrderLine("b", 2))

        assertTrue(ContainsSku("b").isSatisfiedBy(order(lines = lines)))
        assertFalse(ContainsSku("c").isSatisfiedBy(order(lines = lines)))
    }

    @Test
    fun `specifications of one class and definition are equal`() {
        assertEquals(TotalAtLeast(100), TotalAtLeast(100))
        assertEquals(TotalAtLeast(100).hashCode(), TotalAtLeast(100).hashCode())
        assertNotEquals(TotalAtLeast(100), TotalAtLeast(200))
    }

    @Test
    fun `a plain class is equal by its definition too`() {
        assertEquals(WithoutEmail(), WithoutEmail())
    }

    @Test
    fun `the definition is a condition by the path and the constraint`() {
        assertEquals("customer.name = ann", OfCustomer("ann").definition.toString())
    }

    @Test
    fun `a specification is named by its class`() {
        assertEquals("IsOpen", IsOpen.toString())
        assertEquals("WithoutEmail", WithoutEmail().toString())
    }
}
