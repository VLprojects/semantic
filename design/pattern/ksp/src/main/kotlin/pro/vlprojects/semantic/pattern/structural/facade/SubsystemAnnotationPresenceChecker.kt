package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.hasAnnotation

internal class SubsystemAnnotationPresenceChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration, annotation: FacadeAnnotation) {
        val facadeName = declaration.getShortName()

        annotation
            .getSubsystems()
            .filterNot { subsystem -> subsystem.hasAnnotation(Facade.Subsystem::class) }
            .map(KSClassDeclaration::getShortName)
            .forEach { subsystemName ->
                this.logger.error(
                    "Facade $facadeName names @$subsystemName, which is not marked with @Facade.Subsystem",
                    annotation.symbol,
                )
            }
    }
}
