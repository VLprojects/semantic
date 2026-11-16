package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSTypeReference

internal class SubsystemTypeChecker(private val logger: KSPLogger) {

    fun check(subject: String, type: KSTypeReference, allowedNames: Set<String>) = type
        .findDependencies()
        .forEach { dependency ->
            val dependencyName = dependency.readName()

            dependency
                .findCarriedMarkers()
                .filterNot { marker -> marker.readQualifiedName() in allowedNames }
                .map(SubsystemMarker::describe)
                .forEach { markerName ->
                    this.logger.error("$subject names $dependencyName of $markerName past a facade", type)
                }
        }
}
