package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

internal class FacadeChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration, annotation: KSAnnotation) {
        this.checkTarget(declaration, annotation)
        this.checkSubsystems(declaration, annotation)
        this.checkMarkers(declaration)
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
            .findMarkers()
            .forEach { marker ->
                val markerName = marker.readName()

                this.logger.error(
                    "Facade $facadeName carries @$markerName: a facade stands outside its subsystems",
                    marker,
                )
            }
    }

    private fun reportMisplaced(annotation: KSAnnotation, target: String) = this.logger.error(
        "@Facade goes on a class, not on $target",
        annotation,
    )
}
