package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class FacadeSubsystemsNotEmptyChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration, annotation: FacadeAnnotation) {
        if (!annotation.hasSubsystems()) {
            val facadeName = declaration.getShortName()

            this.logger.error("Facade $facadeName names no subsystem", annotation.symbol)
        }
    }
}
