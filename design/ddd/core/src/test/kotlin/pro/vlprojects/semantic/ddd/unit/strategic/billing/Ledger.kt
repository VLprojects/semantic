package pro.vlprojects.semantic.ddd.unit.strategic.billing

import pro.vlprojects.semantic.ddd.strategic.BusinessLogicPattern
import pro.vlprojects.semantic.ddd.strategic.Subdomain

object Ledger : Subdomain.Core {

    override val businessLogicPattern = BusinessLogicPattern.EVENT_SOURCED_DOMAIN_MODEL
}
