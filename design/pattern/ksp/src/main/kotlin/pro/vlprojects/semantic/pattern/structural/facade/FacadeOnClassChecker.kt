package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class FacadeOnClassChecker(private val logger: KSPLogger) {

    fun check(declaration: KSClassDeclaration, annotation: FacadeAnnotation) {
        val facadeName = declaration.getShortName()

        when (declaration.classKind) {
            ClassKind.INTERFACE -> this.reportMisplaced(annotation, "the interface $facadeName")
            ClassKind.ANNOTATION_CLASS -> this.reportMisplaced(annotation, "the annotation $facadeName")
            else -> Unit
        }
    }

    private fun reportMisplaced(annotation: FacadeAnnotation, target: String) = this.logger.error(
        "@Facade goes on a class, not on $target",
        annotation.symbol,
    )
}
