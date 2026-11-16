package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument

public fun KSAnnotation.findAnnotationClass(): KSClassDeclaration? = when (val declaration = this.readDeclaration()) {
    is KSTypeAlias -> declaration.findActualType()
    else -> declaration as? KSClassDeclaration
}

public fun KSAnnotation.readArgumentValues(name: String): List<Any?> = this.arguments
    .filter { argument -> argument.name?.asString() == name }
    .flatMap(KSValueArgument::readValues)

private fun KSAnnotation.readDeclaration() = this.annotationType
    .resolve()
    .declaration

// Java passes a single value of an array attribute without braces as the value itself.
private fun KSValueArgument.readValues() = when (val value = this.value) {
    is List<*> -> value
    else -> listOf(value)
}
