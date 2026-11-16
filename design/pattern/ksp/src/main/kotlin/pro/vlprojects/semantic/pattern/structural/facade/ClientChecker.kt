package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class ClientChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration) {
        val markers = declaration.findMarkers()

        if (markers.size > 1) {
            val name = declaration.readName()
            val names = markers.joinToString { marker -> "@${marker.readName()}" }

            markers.last().let { marker ->
                this.logger.error("$name carries the markers of several subsystems: $names", marker)
            }
        }
    }
}
