package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class FacadeOutsideSubsystemsChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration) {
        val facadeName = declaration.getShortName()

        SubsystemMembership
            .findAll(declaration)
            .forEach { membership ->
                val subsystemName = membership.describe()

                this.logger.error(
                    "Facade $facadeName carries $subsystemName: a facade stands outside its subsystems",
                    membership.annotation,
                )
            }
    }
}
