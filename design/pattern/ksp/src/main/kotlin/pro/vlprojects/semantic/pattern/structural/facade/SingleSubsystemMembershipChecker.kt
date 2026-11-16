package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.hasAnnotation

internal class SingleSubsystemMembershipChecker(private val logger: KSPLogger) {

    // A marker on a facade is reported as a facade inside a subsystem.
    fun check(declaration: KSClassDeclaration) {
        if (!declaration.hasAnnotation(Facade::class)) {
            this.checkMemberships(declaration)
        }
    }

    private fun checkMemberships(declaration: KSClassDeclaration) {
        val memberships = SubsystemMembership.findAll(declaration)

        if (memberships.size > 1) {
            val name = declaration.getShortName()
            val subsystemNames = memberships.joinToString(transform = SubsystemMembership::describe)

            val symbol = memberships
                .lastOrNull(SubsystemMembership::isDirectlyPresent)
                ?.annotation
                ?: declaration

            this.logger.error("$name carries the markers of several subsystems: $subsystemNames", symbol)
        }
    }
}
