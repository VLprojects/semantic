package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile

internal class FacadeSymbolProcessor(private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getNewFiles()
            .flatMap(KSFile::declarations)
            .filterIsInstance<KSClassDeclaration>()
            .flatMap(KSClassDeclaration::flattenClasses)
            .forEach(::checkClass)

        return emptyList()
    }

    private fun checkClass(declaration: KSClassDeclaration) {
        when (val annotation = declaration.findFacadeAnnotation()) {
            null -> this.checkMarkers(declaration)
            else -> this.checkFacade(declaration, annotation)
        }
    }

    private fun checkMarkers(declaration: KSClassDeclaration) {
        val markers = declaration.findMarkers()

        if (markers.size > 1) {
            val name = declaration.readName()
            val names = markers.joinToString { marker -> "@${marker.readName()}" }

            markers.last().let { marker ->
                this.logger.error("$name carries the markers of several subsystems: $names", marker)
            }
        }
    }

    private fun checkFacade(declaration: KSClassDeclaration, annotation: KSAnnotation) {
        this.checkTarget(declaration, annotation)
        this.checkSubsystems(declaration, annotation)
        this.checkFacadeMarkers(declaration)
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

    private fun checkFacadeMarkers(declaration: KSClassDeclaration) {
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
