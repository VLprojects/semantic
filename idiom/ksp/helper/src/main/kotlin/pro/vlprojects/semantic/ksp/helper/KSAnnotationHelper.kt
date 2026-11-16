package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument

public fun KSAnnotation.getAnnotationClass(): KSClassDeclaration? =
    when (val declaration = this.resolveAnnotationDeclaration()) {
        is KSTypeAlias -> declaration.findActualType()
        else -> declaration as? KSClassDeclaration
    }

public fun KSAnnotation.getClassArray(attributeName: String): List<KSType> = this.arguments
    .filter { argument -> argument.name?.asString() == attributeName }
    .flatMap(KSValueArgument::toValueList)
    .filterIsInstance<KSType>()

private fun KSAnnotation.resolveAnnotationDeclaration() = this.annotationType
    .resolve()
    .declaration

// Java passes a single value of an array attribute without braces as the value itself.
private fun KSValueArgument.toValueList() = when (val value = this.value) {
    is List<*> -> value
    else -> listOf(value)
}
