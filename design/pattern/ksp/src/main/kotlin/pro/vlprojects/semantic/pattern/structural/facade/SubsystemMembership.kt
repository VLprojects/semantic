package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pro.vlprojects.semantic.ksp.helper.getQualifiedName
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.hasAnnotation
import pro.vlprojects.semantic.ksp.helper.resolveClassDeclaration

internal class SubsystemMembership private constructor(
    val annotation: KSAnnotation,
    private val subsystem: KSClassDeclaration,
) {

    fun getSubsystemName() = this.subsystem.getQualifiedName()

    fun describe() = "@${this.subsystem.getShortName()}"

    companion object Factory {

        fun findAll(declaration: KSClassDeclaration) = declaration.annotations
            .mapNotNull(::createOrNull)
            .distinctBy(SubsystemMembership::getSubsystemName)
            .toList()

        private fun createOrNull(annotation: KSAnnotation) = annotation
            .resolveClassDeclaration()
            ?.takeIf { subsystem -> subsystem.hasAnnotation(Facade.Subsystem::class) }
            ?.let { subsystem -> SubsystemMembership(annotation, subsystem) }
    }
}
