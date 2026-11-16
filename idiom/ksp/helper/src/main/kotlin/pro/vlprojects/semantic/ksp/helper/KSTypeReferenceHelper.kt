package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference

/**
 * The classes this type refers to, each once: its own, of its arguments, behind type aliases, in bounds.
 *
 * ```
 * val files: Map<String, List<FileEntity>>
 * fun <T : Comparable<T>> latest(): T
 *
 * files.type.getReferencedClasses()        // [String, FileEntity, List, Map]
 * latest.returnType.getReferencedClasses() // [Comparable]
 * ```
 */
public fun KSTypeReference.getReferencedClasses(): Sequence<KSClassDeclaration> =
    this.getReferencedClasses(emptySet()).distinct()

private fun KSTypeReference.getReferencedClasses(visitedParameters: Set<KSTypeParameter>) =
    this.resolve().getReferencedClasses(visitedParameters)

private fun KSType.getReferencedClasses(visitedParameters: Set<KSTypeParameter>): Sequence<KSClassDeclaration> {
    val argumentClasses = this.arguments
        .asSequence()
        .mapNotNull(KSTypeArgument::type)
        .flatMap { argument -> argument.getReferencedClasses(visitedParameters) }

    val declarationClasses = this.declaration.getReferencedClasses(visitedParameters)

    return argumentClasses + declarationClasses
}

private fun KSDeclaration.getReferencedClasses(visitedParameters: Set<KSTypeParameter>) = when (this) {
    is KSClassDeclaration -> sequenceOf(this)
    is KSTypeAlias -> this.type.getReferencedClasses(visitedParameters)
    is KSTypeParameter -> this.getBoundClasses(visitedParameters)
    else -> emptySequence()
}

// A visited parameter is skipped: a bound as T : Comparable<T> leads back to its own parameter.
private fun KSTypeParameter.getBoundClasses(visitedParameters: Set<KSTypeParameter>) = when (this) {
    in visitedParameters -> emptySequence()
    else -> this.bounds.flatMap { bound -> bound.getReferencedClasses(visitedParameters + this) }
}
