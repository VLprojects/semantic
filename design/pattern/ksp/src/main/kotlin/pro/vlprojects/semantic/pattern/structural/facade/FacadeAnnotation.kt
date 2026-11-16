package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import pro.vlprojects.semantic.ksp.helper.getFirstAnnotationOrNull
import pro.vlprojects.semantic.ksp.helper.getTypeValues
import pro.vlprojects.semantic.ksp.helper.resolveClassDeclaration

internal class FacadeAnnotation private constructor(val symbol: KSAnnotation) {

    fun hasSubsystems() = this
        .getSubsystemTypes()
        .isNotEmpty()

    // An unresolved class is left to the compiler, which names it better.
    fun getSubsystems() = this
        .getSubsystemTypes()
        .filterNot(KSType::isError)
        .mapNotNull(KSType::resolveClassDeclaration)

    private fun getSubsystemTypes() = this.symbol.getTypeValues(Facade::subsystems.name)

    companion object Factory {

        fun find(declaration: KSAnnotated) = declaration
            .getFirstAnnotationOrNull(Facade::class)
            ?.let(::FacadeAnnotation)
    }
}
