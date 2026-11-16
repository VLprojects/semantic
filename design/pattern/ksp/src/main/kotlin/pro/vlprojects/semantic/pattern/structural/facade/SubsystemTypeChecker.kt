package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSTypeReference
import pro.vlprojects.semantic.ksp.helper.getReferencedClasses
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class SubsystemTypeChecker(private val logger: KSPLogger) {

    fun check(subject: String, type: KSTypeReference, allowedNames: Set<String>) = type
        .getReferencedClasses()
        .forEach { dependency ->
            val dependencyName = dependency.getShortName()

            dependency
                .findCarriedMarkers()
                .filterNot { marker -> marker.getQualifiedName() in allowedNames }
                .map(SubsystemMarker::describe)
                .forEach { markerName ->
                    this.logger.error("$subject names $dependencyName of $markerName past a facade", type)
                }
        }
}
