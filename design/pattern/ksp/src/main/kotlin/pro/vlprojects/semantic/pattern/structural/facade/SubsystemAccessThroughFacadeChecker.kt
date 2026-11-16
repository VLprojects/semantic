package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import pro.vlprojects.semantic.ksp.helper.getReferencedClasses
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class SubsystemAccessThroughFacadeChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration) {
        when (val annotation = FacadeAnnotation.find(declaration)) {
            null -> this.checkClass(declaration)
            else -> this.checkFacade(declaration, annotation)
        }
    }

    fun check(declaration: KSFunctionDeclaration) = this
        .findFunctionTypes(declaration)
        .let { types -> this.checkTopLevel(declaration, types) }

    fun check(declaration: KSPropertyDeclaration) = this
        .findPropertyTypes(declaration)
        .let { types -> this.checkTopLevel(declaration, types) }

    private fun checkFacade(declaration: KSClassDeclaration, annotation: FacadeAnnotation) {
        val facadeName = declaration.getShortName()
        val subsystemNames = annotation.getSubsystemNames()

        MemberSignatureTypes
            .of(declaration)
            .findAll()
            .forEach { type -> this.checkType("Facade $facadeName", type, subsystemNames) }
    }

    // A companion is checked with the class it belongs to, as a part of it.
    private fun checkClass(declaration: KSClassDeclaration) {
        if (!declaration.isCompanionObject) {
            this.checkMemberTypes(declaration)
        }
    }

    private fun checkMemberTypes(declaration: KSClassDeclaration) {
        val className = declaration.getShortName()

        val subsystemNames = SubsystemMembership
            .findAll(declaration)
            .map(SubsystemMembership::getSubsystemName)
            .toSet()

        MemberSignatureTypes
            .of(declaration)
            .findAll()
            .forEach { type -> this.checkType(className, type, subsystemNames) }
    }

    private fun checkTopLevel(declaration: KSDeclaration, types: Sequence<KSTypeReference>) {
        val name = declaration.getShortName()

        types.forEach { type -> this.checkType(name, type, emptySet()) }
    }

    private fun checkType(subject: String, type: KSTypeReference, allowedNames: Set<String>) = type
        .getReferencedClasses()
        .forEach { dependency ->
            val dependencyName = dependency.getShortName()

            SubsystemMembership
                .findAll(dependency)
                .filterNot { membership -> membership.getSubsystemName() in allowedNames }
                .map(SubsystemMembership::describe)
                .forEach { subsystemName ->
                    this.logger.error("$subject names $dependencyName of $subsystemName past a facade", type)
                }
        }

    private fun findFunctionTypes(function: KSFunctionDeclaration) = function.parameters
        .map(KSValueParameter::type)
        .plus(function.returnType)
        .plus(function.extensionReceiver)
        .filterNotNull()
        .asSequence()

    private fun findPropertyTypes(property: KSPropertyDeclaration) =
        sequenceOf(property.type, property.extensionReceiver).filterNotNull()
}
