package pro.vlprojects.semantic.ddd.unit.strategic.billing

import pro.vlprojects.semantic.ddd.strategic.BoundedContext
import pro.vlprojects.semantic.ddd.unit.strategic.Invoicing
import pro.vlprojects.semantic.ddd.unit.strategic.TaxCalculation

object Billing : BoundedContext {

    override val subdomains = setOf(Invoicing, TaxCalculation)
}
