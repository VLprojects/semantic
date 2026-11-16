package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class ClientChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration) {
        val markers = declaration.findCarriedMarkers()

        if (markers.size > 1) {
            val name = declaration.readName()
            val names = markers.joinToString(transform = SubsystemMarker::describe)

            val symbol = markers
                .lastOrNull(SubsystemMarker::isDirectlyPresent)
                ?.annotation
                ?: declaration

            this.logger.error("$name carries the markers of several subsystems: $names", symbol)
        }
    }
}
