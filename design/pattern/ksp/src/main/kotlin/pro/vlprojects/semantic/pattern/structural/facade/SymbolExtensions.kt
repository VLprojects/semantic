package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument
import kotlin.reflect.KClass

internal fun KSClassDeclaration.flattenClasses(): Sequence<KSClassDeclaration> = this.declarations
    .filterIsInstance<KSClassDeclaration>()
    .flatMap(KSClassDeclaration::flattenClasses)
    .let { nested -> sequenceOf(this) + nested }

internal fun KSAnnotated.findMarkers() = this.annotations
    .filter { annotation -> annotation.resolveDeclaration().isMarker() }
    .distinctBy { annotation -> annotation.resolveDeclaration().readQualifiedName() }
    .toList()

internal fun KSAnnotated.findFacadeAnnotation() = this.annotations
    .firstOrNull { annotation -> annotation.isOf(Facade::class) }

internal fun KSAnnotation.hasSubsystemArguments() = this
    .readSubsystemArguments()
    .isNotEmpty()

// An unresolved class is left to the compiler, which names it better.
internal fun KSAnnotation.readSubsystems() = this
    .readSubsystemArguments()
    .filterIsInstance<KSType>()
    .filterNot(KSType::isError)
    .map(KSType::declaration)
    .map(KSDeclaration::expandTypeAlias)

internal fun KSAnnotation.readName() = this
    .resolveDeclaration()
    .readName()

internal fun KSDeclaration.isMarker() = this.annotations
    .any { annotation -> annotation.isOf(Facade.Subsystem::class) }

private fun KSDeclaration.readQualifiedName() = this.qualifiedName
    ?.asString()
    .orEmpty()

internal fun KSDeclaration.readName() = this.packageName
    .asString()
    .let { packageName -> this.qualifiedName?.asString()?.removePrefix("$packageName.") }
    ?: this.simpleName.asString()

private fun KSAnnotation.readSubsystemArguments() = this.arguments
    .filter { argument -> argument.name?.asString() == Facade::subsystems.name }
    .flatMap(KSValueArgument::readValues)

// Java passes a single value of an array attribute without braces as the value itself.
private fun KSValueArgument.readValues() = when (val value = this.value) {
    is List<*> -> value
    else -> listOf(value)
}

private fun KSAnnotation.isOf(type: KClass<out Annotation>) = this
    .resolveDeclaration()
    .readQualifiedName() == type.qualifiedName

private fun KSAnnotation.resolveDeclaration() = this.annotationType
    .resolve()
    .declaration
    .expandTypeAlias()

private fun KSDeclaration.expandTypeAlias() = when (this) {
    is KSTypeAlias -> this.findActualType()
    else -> this
}
