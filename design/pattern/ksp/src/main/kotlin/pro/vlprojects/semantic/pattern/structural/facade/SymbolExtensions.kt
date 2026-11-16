package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Origin
import kotlin.reflect.KClass

internal fun KSClassDeclaration.flattenClasses(): Sequence<KSClassDeclaration> = this.declarations
    .filterIsInstance<KSClassDeclaration>()
    .flatMap(KSClassDeclaration::flattenClasses)
    .let { nested -> sequenceOf(this) + nested }

internal fun KSClassDeclaration.findVisibleTypes(): Sequence<KSTypeReference> {
    val functionTypes = this
        .getDeclaredFunctions()
        .filter(KSFunctionDeclaration::isVisibleToClients)
        .filterNot(KSFunctionDeclaration::isConstructor)
        .flatMap(KSFunctionDeclaration::findSignatureTypes)

    val propertyTypes = this
        .getDeclaredProperties()
        .filter(KSPropertyDeclaration::isVisibleToClients)
        .flatMap(KSPropertyDeclaration::findSignatureTypes)

    val companionTypes = this.declarations
        .filterIsInstance<KSClassDeclaration>()
        .filter(KSClassDeclaration::isCompanionObject)
        .filter(KSClassDeclaration::isVisibleToClients)
        .flatMap(KSClassDeclaration::findVisibleTypes)

    return functionTypes + propertyTypes + companionTypes
}

private fun KSFunctionDeclaration.findSignatureTypes() = this.parameters
    .map(KSValueParameter::type)
    .plus(this.returnType)
    .plus(this.extensionReceiver)
    .filterNotNull()
    .asSequence()

private fun KSPropertyDeclaration.findSignatureTypes() = sequenceOf(this.type, this.extensionReceiver)
    .filterNotNull()

// Java opens a protected member to its whole package, wider than a package-private one.
private fun KSDeclaration.isVisibleToClients() = when {
    this.isPrivate() -> false
    this.origin == Origin.JAVA -> true
    this.isProtected() -> false
    else -> true
}

internal fun KSTypeReference.findClasses() = this.findClasses(emptySet())

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

internal fun KSAnnotated.findMarkers() = this.annotations
    .filter { annotation -> annotation.resolveDeclaration().isMarker() }
    .distinctBy(KSAnnotation::readQualifiedName)
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

internal fun KSAnnotation.readQualifiedName() = this
    .resolveDeclaration()
    .readQualifiedName()

internal fun KSAnnotation.readName() = this
    .resolveDeclaration()
    .readName()

internal fun KSDeclaration.isMarker() = this.annotations
    .any { annotation -> annotation.isOf(Facade.Subsystem::class) }

internal fun KSDeclaration.readQualifiedName() = this.qualifiedName
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

private fun KSAnnotation.isOf(type: KClass<out Annotation>) = this.readQualifiedName() == type.qualifiedName

private fun KSAnnotation.resolveDeclaration() = this.annotationType
    .resolve()
    .declaration
    .expandTypeAlias()

private fun KSDeclaration.expandTypeAlias() = when (this) {
    is KSTypeAlias -> this.findActualType()
    else -> this
}
