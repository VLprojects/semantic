package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference

public fun KSTypeReference.findAllInvolvedClasses(): Sequence<KSClassDeclaration> = this
    .findClasses(emptySet())
    .distinct()

private fun KSTypeReference.findClasses(visited: Set<KSTypeParameter>) = this
    .resolve()
    .findClasses(visited)

private fun KSType.findClasses(visited: Set<KSTypeParameter>): Sequence<KSClassDeclaration> {
    val argumentClasses = this.arguments
        .asSequence()
        .mapNotNull(KSTypeArgument::type)
        .flatMap { argument -> argument.findClasses(visited) }

    return when (val declaration = this.declaration) {
        is KSClassDeclaration -> argumentClasses + declaration
        is KSTypeAlias -> argumentClasses + declaration.findActualClasses(visited)
        is KSTypeParameter -> argumentClasses + declaration.findBoundClasses(visited)
        else -> argumentClasses
    }
}

private fun KSTypeAlias.findActualClasses(visited: Set<KSTypeParameter>) = this.type.findClasses(visited)

// A visited parameter is skipped: a bound as T : Comparable<T> leads back to its own parameter.
private fun KSTypeParameter.findBoundClasses(visited: Set<KSTypeParameter>) = when (this) {
    in visited -> emptySequence()
    else -> this.bounds.flatMap { bound -> bound.findClasses(visited + this) }
}
