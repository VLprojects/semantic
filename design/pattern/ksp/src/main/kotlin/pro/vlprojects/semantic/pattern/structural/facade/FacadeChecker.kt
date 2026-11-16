package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference

internal class FacadeChecker(private val logger: KSPLogger, private val subsystemTypeChecker: SubsystemTypeChecker) {

    fun check(declaration: KSClassDeclaration, annotation: KSAnnotation) {
        this.checkTarget(declaration, annotation)
        this.checkSubsystems(declaration, annotation)
        this.checkMarkers(declaration)
        this.checkTypes(declaration, annotation)
    }

    private fun checkTarget(declaration: KSClassDeclaration, annotation: KSAnnotation) {
        val facadeName = declaration.readName()

        when (declaration.classKind) {
            ClassKind.INTERFACE -> this.reportMisplaced(annotation, "the interface $facadeName")
            ClassKind.ANNOTATION_CLASS -> this.reportMisplaced(annotation, "the annotation $facadeName")
            else -> Unit
        }
    }

    private fun checkSubsystems(declaration: KSClassDeclaration, annotation: KSAnnotation) {
        val facadeName = declaration.readName()

        if (!annotation.hasSubsystemArguments()) {
            this.logger.error("Facade $facadeName names no subsystem", annotation)
        }

        annotation
            .readSubsystems()
            .filterNot(KSDeclaration::isMarker)
            .map(KSDeclaration::readName)
            .forEach { subsystemName ->
                this.logger.error(
                    "Facade $facadeName names @$subsystemName, which is not marked with @Facade.Subsystem",
                    annotation,
                )
            }
    }

    private fun checkMarkers(declaration: KSClassDeclaration) {
        val facadeName = declaration.readName()

        declaration
            .findCarriedMarkers()
            .forEach { marker ->
                val markerName = marker.describe()

                this.logger.error(
                    "Facade $facadeName carries $markerName: a facade stands outside its subsystems",
                    marker.findSymbol(declaration),
                )
            }
    }

    private fun checkTypes(declaration: KSClassDeclaration, annotation: KSAnnotation) {
        val facadeName = declaration.readName()

        val subsystemNames = annotation
            .readSubsystems()
            .map(KSDeclaration::readQualifiedName)
            .toSet()

        declaration
            .findSignatureTypes()
            .forEach { type -> this.subsystemTypeChecker.check("Facade $facadeName", type, subsystemNames) }

        declaration
            .findVisibleTypes()
            .forEach { type -> this.checkVisibleType(facadeName, type, subsystemNames) }
    }

    private fun checkVisibleType(facadeName: String, type: KSTypeReference, subsystemNames: Set<String>) = type
        .findDependencies()
        .forEach { dependency ->
            val dependencyName = dependency.readName()

            dependency
                .findCarriedMarkers()
                .filter { marker -> marker.readQualifiedName() in subsystemNames }
                .map(SubsystemMarker::describe)
                .forEach { markerName ->
                    this.logger.error("Facade $facadeName exposes $dependencyName of $markerName", type)
                }
        }

    private fun reportMisplaced(annotation: KSAnnotation, target: String) = this.logger.error(
        "@Facade goes on a class, not on $target",
        annotation,
    )
}
