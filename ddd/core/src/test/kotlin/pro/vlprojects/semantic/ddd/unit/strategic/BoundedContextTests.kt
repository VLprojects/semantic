package pro.vlprojects.semantic.ddd.unit.strategic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.strategic.contains
import pro.vlprojects.semantic.ddd.strategic.packageName
import pro.vlprojects.semantic.ddd.unit.strategic.billing.Billing
import pro.vlprojects.semantic.ddd.unit.strategic.billing.Invoice
import pro.vlprojects.semantic.ddd.unit.strategic.billing.payment.Payment
import pro.vlprojects.semantic.ddd.unit.strategic.billingreport.BillingReport

class BoundedContextTests {

    @Test
    fun `a context is named after its class in kebab case`() {
        assertEquals("billing", Billing.name)
        assertEquals("order-fulfillment", OrderFulfillment.name)
    }

    @Test
    fun `a context may name itself`() {
        assertEquals("delivery", Shipping.name)
    }

    @Test
    fun `a context lives in the package of its object`() {
        assertEquals("pro.vlprojects.semantic.ddd.unit.strategic.billing", Billing.packageName)
    }

    @Test
    fun `a context holds the types of its package`() {
        assertTrue(Invoice::class in Billing)
    }

    @Test
    fun `a context holds the types of its subpackages`() {
        assertTrue(Payment::class in Billing)
    }

    @Test
    fun `a context does not hold a package that only starts with its name`() {
        assertFalse(BillingReport::class in Billing)
    }

    @Test
    fun `a context does not hold the package above it`() {
        assertFalse(BoundedContextTests::class in Billing)
    }
}
