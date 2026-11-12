package pro.vlprojects.semantic.ddd.unit.strategic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.strategic.ArchitecturalPattern
import pro.vlprojects.semantic.ddd.strategic.BusinessLogicPattern
import pro.vlprojects.semantic.ddd.strategic.Complexity
import pro.vlprojects.semantic.ddd.strategic.Subdomain
import pro.vlprojects.semantic.ddd.strategic.TestingStrategy
import pro.vlprojects.semantic.ddd.strategic.Volatility
import pro.vlprojects.semantic.ddd.unit.strategic.billing.Invoicing
import pro.vlprojects.semantic.ddd.unit.strategic.billing.Ledger
import pro.vlprojects.semantic.ddd.unit.strategic.billing.TaxCalculation

class SubdomainCharacteristicsTests {

    @Test
    fun `a core subdomain is complex and volatile, a domain model behind ports and adapters`() {
        assertEquals(
            listOf(
                Complexity.HIGH,
                Volatility.HIGH,
                BusinessLogicPattern.DOMAIN_MODEL,
                ArchitecturalPattern.PORTS_AND_ADAPTERS,
                TestingStrategy.PYRAMID,
            ),
            Pricing.characteristics,
        )
    }

    @Test
    fun `a supporting subdomain is simple and stable, a transaction script in layers`() {
        assertEquals(
            listOf(
                Complexity.LOW,
                Volatility.LOW,
                BusinessLogicPattern.TRANSACTION_SCRIPT,
                ArchitecturalPattern.LAYERED,
                TestingStrategy.REVERSED_PYRAMID,
            ),
            Invoicing.characteristics,
        )
    }

    @Test
    fun `a generic subdomain is complex and stable, a domain model behind ports and adapters`() {
        assertEquals(
            listOf(
                Complexity.HIGH,
                Volatility.LOW,
                BusinessLogicPattern.DOMAIN_MODEL,
                ArchitecturalPattern.PORTS_AND_ADAPTERS,
                TestingStrategy.PYRAMID,
            ),
            TaxCalculation.characteristics,
        )
    }

    @Test
    fun `a subdomain that overrides its complexity gets the pattern of business logic that follows`() {
        assertEquals(BusinessLogicPattern.DOMAIN_MODEL, Routing.businessLogicPattern)
    }

    @Test
    fun `a subdomain that overrides its pattern of business logic gets the architecture and tests that follow`() {
        assertEquals(
            listOf(ArchitecturalPattern.CQRS, TestingStrategy.PYRAMID),
            listOf(Ledger.architecturalPattern, Ledger.testingStrategy),
        )
    }

    @Test
    fun `an active record lives in layers and is tested by a diamond`() {
        assertEquals(
            listOf(ArchitecturalPattern.LAYERED, TestingStrategy.DIAMOND),
            listOf(Catalog.architecturalPattern, Catalog.testingStrategy),
        )
    }

    private val Subdomain.characteristics
        get() = listOf(
            this.complexity,
            this.volatility,
            this.businessLogicPattern,
            this.architecturalPattern,
            this.testingStrategy,
        )
}
