package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference

public fun KSTypeReference.getAllInvolvedClasses(): Sequence<KSClassDeclaration> = this
    .getInvolvedClasses(emptySet())
    .distinct()

private fun KSTypeReference.getInvolvedClasses(visited: Set<KSTypeParameter>) = this
    .resolve()
    .getInvolvedClasses(visited)

private fun KSType.getInvolvedClasses(visited: Set<KSTypeParameter>): Sequence<KSClassDeclaration> {
    val argumentClasses = this.arguments
        .asSequence()
        .mapNotNull(KSTypeArgument::type)
        .flatMap { argument -> argument.getInvolvedClasses(visited) }

    return when (val declaration = this.declaration) {
        is KSClassDeclaration -> argumentClasses + declaration
        is KSTypeAlias -> argumentClasses + declaration.getExpandedClasses(visited)
        is KSTypeParameter -> argumentClasses + declaration.getBoundClasses(visited)
        else -> argumentClasses
    }
}

private fun KSTypeAlias.getExpandedClasses(visited: Set<KSTypeParameter>) = this.type.getInvolvedClasses(visited)

// A visited parameter is skipped: a bound as T : Comparable<T> leads back to its own parameter.
private fun KSTypeParameter.getBoundClasses(visited: Set<KSTypeParameter>) = when (this) {
    in visited -> emptySequence()
    else -> this.bounds.flatMap { bound -> bound.getInvolvedClasses(visited + this) }
}
