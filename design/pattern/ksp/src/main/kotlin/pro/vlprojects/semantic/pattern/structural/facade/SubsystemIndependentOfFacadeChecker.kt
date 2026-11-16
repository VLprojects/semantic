package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import pro.vlprojects.semantic.ksp.helper.getReferencedClasses
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.hasAnnotation

internal class SubsystemIndependentOfFacadeChecker(private val logger: KSPLogger) {

    // A companion is checked with the class it belongs to, as a part of it.
    fun check(declaration: KSClassDeclaration) {
        when {
            declaration.isCompanionObject -> Unit
            declaration.hasAnnotation(Facade::class) -> Unit
            else -> this.checkTakenTypes(declaration)
        }
    }

    private fun checkTakenTypes(declaration: KSClassDeclaration) {
        val className = declaration.getShortName()
        val memberships = SubsystemMembership.findAll(declaration)

        MemberSignatureTypes
            .of(declaration)
            .findTaken()
            .forEach { type -> this.checkTakenType(className, type, memberships) }
    }

    private fun checkTakenType(className: String, type: KSTypeReference, memberships: List<SubsystemMembership>) = type
        .getReferencedClasses()
        .forEach { dependency ->
            val dependencyName = dependency.getShortName()

            val facadeSubsystemNames = FacadeAnnotation
                .find(dependency)
                ?.getSubsystemNames()
                .orEmpty()

            memberships
                .filter { membership -> membership.getSubsystemName() in facadeSubsystemNames }
                .map(SubsystemMembership::describe)
                .forEach { subsystemName ->
                    this.logger.error("$className of $subsystemName takes its facade $dependencyName", type)
                }
        }
}
