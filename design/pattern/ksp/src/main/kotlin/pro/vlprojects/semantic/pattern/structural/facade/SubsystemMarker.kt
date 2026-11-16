package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pro.vlprojects.semantic.ksp.helper.getShortName

internal class SubsystemMarker(
    val annotation: KSAnnotation,
    private val source: KSClassDeclaration,
    private val aggregateIndex: Int,
) {

    fun getQualifiedName() = this.annotation.getQualifiedName()

    fun isDirectlyPresent() = this.aggregateIndex == 0

    fun findSymbol(carrier: KSClassDeclaration) = when {
        this.isDirectlyPresent() -> this.annotation
        else -> carrier
    }

    fun describe() = when {
        this.isDirectlyPresent() -> "@${this.annotation.getShortName()}"
        else -> "@${this.annotation.getShortName()} through ${this.source.getShortName()}"
    }
}
