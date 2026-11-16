package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument

public fun KSAnnotation.resolveClassDeclaration(): KSClassDeclaration? =
    this.annotationType.resolve().declaration.toClassDeclaration()

// Java passes a single value of an array argument without braces as the value itself.
public fun KSAnnotation.getTypeValues(argumentName: String): List<KSType> = this.arguments
    .filter { argument -> argument.name?.asString() == argumentName }
    .map(KSValueArgument::value)
    .flatMap { value -> value as? List<*> ?: listOf(value) }
    .filterIsInstance<KSType>()

private fun KSDeclaration.toClassDeclaration() = when (this) {
    is KSTypeAlias -> this.findActualType()
    else -> this as? KSClassDeclaration
}
