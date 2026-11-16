package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import pro.vlprojects.semantic.ksp.helper.getReferencedClasses
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class SubsystemTypesHiddenByFacadeChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration, annotation: FacadeAnnotation) {
        val facadeName = declaration.getShortName()
        val subsystemNames = annotation.getSubsystemNames()

        MemberSignatureTypes
            .of(declaration)
            .findVisible()
            .forEach { type -> this.checkVisibleType(facadeName, type, subsystemNames) }
    }

    private fun checkVisibleType(facadeName: String, type: KSTypeReference, subsystemNames: Set<String>) = type
        .getReferencedClasses()
        .forEach { dependency ->
            val dependencyName = dependency.getShortName()

            SubsystemMembership
                .findAll(dependency)
                .filter { membership -> membership.getSubsystemName() in subsystemNames }
                .map(SubsystemMembership::describe)
                .forEach { subsystemName ->
                    this.logger.error("Facade $facadeName exposes $dependencyName of $subsystemName", type)
                }
        }
}
