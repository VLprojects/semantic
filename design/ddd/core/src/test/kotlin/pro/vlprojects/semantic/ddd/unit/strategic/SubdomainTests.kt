package pro.vlprojects.semantic.ddd.unit.strategic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.strategic.Subdomain
import pro.vlprojects.semantic.ddd.unit.strategic.billing.Billing

class SubdomainTests {

    @Test
    fun `a subdomain is named after its class in kebab case`() {
        assertEquals("tax-calculation", TaxCalculation.name)
    }

    @Test
    fun `a context serves no subdomain until it names one`() {
        assertTrue(OrderFulfillment.subdomains.isEmpty())
    }

    @Test
    fun `a context serves the subdomains it names`() {
        assertEquals(setOf(Invoicing, TaxCalculation), Billing.subdomains)
    }

    @Test
    fun `the subdomains of a context are picked by their kind`() {
        assertEquals(listOf(TaxCalculation), Billing.subdomains.filterIsInstance<Subdomain.Generic>())
    }

    @Test
    fun `a subdomain is of one of three kinds`() {
        val kinds = setOf(Pricing, Invoicing, TaxCalculation).map {
            when (it) {
                is Subdomain.Core -> "core"
                is Subdomain.Supporting -> "supporting"
                is Subdomain.Generic -> "generic"
            }
        }

        assertEquals(listOf("core", "supporting", "generic"), kinds)
    }
}
