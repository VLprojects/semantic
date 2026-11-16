package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference

public fun KSTypeReference.getReferencedClasses(): Sequence<KSClassDeclaration> = this
    .getReferencedClasses(emptySet())
    .distinct()

private fun KSTypeReference.getReferencedClasses(visitedParameters: Set<KSTypeParameter>) = this
    .resolve()
    .getReferencedClasses(visitedParameters)

private fun KSType.getReferencedClasses(visitedParameters: Set<KSTypeParameter>): Sequence<KSClassDeclaration> {
    val argumentClasses = this.arguments
        .asSequence()
        .mapNotNull(KSTypeArgument::type)
        .flatMap { argument -> argument.getReferencedClasses(visitedParameters) }

    return when (val declaration = this.declaration) {
        is KSClassDeclaration -> argumentClasses + declaration
        is KSTypeAlias -> argumentClasses + declaration.getReferencedClasses(visitedParameters)
        is KSTypeParameter -> argumentClasses + declaration.getReferencedClasses(visitedParameters)
        else -> argumentClasses
    }
}

private fun KSTypeAlias.getReferencedClasses(visitedParameters: Set<KSTypeParameter>) =
    this.type.getReferencedClasses(visitedParameters)

// A visited parameter is skipped: a bound as T : Comparable<T> leads back to its own parameter.
private fun KSTypeParameter.getReferencedClasses(visitedParameters: Set<KSTypeParameter>) = when (this) {
    in visitedParameters -> emptySequence()
    else -> this.bounds.flatMap { bound -> bound.getReferencedClasses(visitedParameters + this) }
}
