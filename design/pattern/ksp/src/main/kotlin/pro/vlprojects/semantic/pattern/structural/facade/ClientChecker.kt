package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import pro.vlprojects.semantic.ksp.helper.getQualifiedName
import pro.vlprojects.semantic.ksp.helper.getReferencedClasses
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class ClientChecker(private val logger: KSPLogger, private val subsystemTypeChecker: SubsystemTypeChecker) {

    // A companion is checked with the class it belongs to, as a part of it.
    fun check(declaration: KSClassDeclaration) {
        when {
            declaration.isCompanionObject -> this.checkCompanion(declaration)
            else -> this.checkClass(declaration)
        }
    }

    fun check(declaration: KSFunctionDeclaration) = this.checkTopLevel(declaration, declaration.findSignatureTypes())

    fun check(declaration: KSPropertyDeclaration) = this.checkTopLevel(declaration, declaration.findSignatureTypes())

    private fun checkTopLevel(declaration: KSDeclaration, types: Sequence<KSTypeReference>) {
        val name = declaration.getShortName()

        types.forEach { type -> this.subsystemTypeChecker.check(name, type, emptySet()) }
    }

    private fun checkCompanion(declaration: KSClassDeclaration) = declaration
        .findCarriedMarkers()
        .let { markers -> this.checkMarkers(declaration, markers) }

    private fun checkClass(declaration: KSClassDeclaration) {
        val clientName = declaration.getShortName()
        val markers = declaration.findCarriedMarkers()

        val markerNames = markers
            .map(SubsystemMarker::getQualifiedName)
            .toSet()

        this.checkMarkers(declaration, markers)

        declaration
            .findSignatureTypes()
            .forEach { type -> this.subsystemTypeChecker.check(clientName, type, markerNames) }

        declaration
            .findTakenTypes()
            .forEach { type -> this.checkOwnFacade(clientName, type, markers) }
    }

    private fun checkMarkers(declaration: KSClassDeclaration, markers: List<SubsystemMarker>) {
        if (markers.size > 1) {
            val name = declaration.getShortName()
            val names = markers.joinToString(transform = SubsystemMarker::describe)

            val symbol = markers
                .lastOrNull(SubsystemMarker::isDirectlyPresent)
                ?.annotation
                ?: declaration

            this.logger.error("$name carries the markers of several subsystems: $names", symbol)
        }
    }

    private fun checkOwnFacade(clientName: String, type: KSTypeReference, markers: List<SubsystemMarker>) = type
        .getReferencedClasses()
        .forEach { dependency ->
            val dependencyName = dependency.getShortName()

            val subsystemNames = dependency
                .findFacadeAnnotation()
                ?.readSubsystems()
                .orEmpty()
                .map(KSDeclaration::getQualifiedName)

            markers
                .filter { marker -> marker.getQualifiedName() in subsystemNames }
                .map(SubsystemMarker::describe)
                .forEach { markerName ->
                    this.logger.error("$clientName of $markerName takes its facade $dependencyName", type)
                }
        }
}
