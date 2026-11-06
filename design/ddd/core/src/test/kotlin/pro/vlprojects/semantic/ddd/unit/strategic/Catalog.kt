package pro.vlprojects.semantic.ddd.unit.strategic

import pro.vlprojects.semantic.ddd.strategic.BusinessLogicPattern
import pro.vlprojects.semantic.ddd.strategic.Subdomain

object Catalog : Subdomain.Supporting {

    override val businessLogicPattern = BusinessLogicPattern.ACTIVE_RECORD
}
