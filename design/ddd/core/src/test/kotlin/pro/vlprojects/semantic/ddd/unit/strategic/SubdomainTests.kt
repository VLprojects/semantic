package pro.vlprojects.semantic.ddd.unit.strategic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.strategic.Subdomain
import pro.vlprojects.semantic.ddd.strategic.hasBoundedContext
import pro.vlprojects.semantic.ddd.unit.strategic.billing.Billing
import pro.vlprojects.semantic.ddd.unit.strategic.billing.Invoicing
import pro.vlprojects.semantic.ddd.unit.strategic.billing.TaxCalculation

class SubdomainTests {

    @Test
    fun `a subdomain is named after its class in kebab case`() {
        assertEquals("tax-calculation", TaxCalculation.name)
    }

    @Test
    fun `a subdomain belongs to the context of its package`() {
        assertTrue(TaxCalculation::class hasBoundedContext Billing)
    }

    @Test
    fun `a subdomain beside the package of a context does not belong to it`() {
        assertFalse(Pricing::class hasBoundedContext Billing)
    }

    @Test
    fun `a subdomain is of one of three categories`() {
        val categories = setOf(Pricing, Invoicing, TaxCalculation).map {
            when (it) {
                is Subdomain.Core -> "core"
                is Subdomain.Supporting -> "supporting"
                is Subdomain.Generic -> "generic"
            }
        }

        assertEquals(listOf("core", "supporting", "generic"), categories)
    }
}
