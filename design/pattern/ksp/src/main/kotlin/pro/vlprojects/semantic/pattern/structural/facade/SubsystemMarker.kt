package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class SubsystemMarker(
    val annotation: KSAnnotation,
    private val source: KSClassDeclaration,
    private val aggregateIndex: Int,
) {

    fun readQualifiedName() = this.annotation.readQualifiedName()

    fun isDirectlyPresent() = this.aggregateIndex == 0

    fun findSymbol(carrier: KSClassDeclaration) = when {
        this.isDirectlyPresent() -> this.annotation
        else -> carrier
    }

    fun describe() = when {
        this.isDirectlyPresent() -> "@${this.annotation.readName()}"
        else -> "@${this.annotation.readName()} through ${this.source.readName()}"
    }
}
