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

internal fun KSClassDeclaration.findSignatureTypes() = this.findTakenTypes() + this.findResultTypes()

internal fun KSClassDeclaration.findTakenTypes(): Sequence<KSTypeReference> {
    val functionTypes = this
        .getDeclaredFunctions()
        .filterNot(KSFunctionDeclaration::isSynthetic)
        .flatMap(KSFunctionDeclaration::findTakenTypes)

    val propertyTypes = this
        .getDeclaredProperties()
        .flatMap(KSPropertyDeclaration::findSignatureTypes)

    val companionTypes = this
        .findCompanions()
        .flatMap(KSClassDeclaration::findTakenTypes)

    return functionTypes + propertyTypes + companionTypes
}

private fun KSClassDeclaration.findResultTypes(): Sequence<KSTypeReference> {
    val functionTypes = this
        .getDeclaredFunctions()
        .filterNot(KSFunctionDeclaration::isSynthetic)
        .filterNot(KSFunctionDeclaration::isConstructor)
        .mapNotNull(KSFunctionDeclaration::returnType)

    val companionTypes = this
        .findCompanions()
        .flatMap(KSClassDeclaration::findResultTypes)

    return functionTypes + companionTypes
}

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

    val companionTypes = this
        .findCompanions()
        .filter(KSClassDeclaration::isVisibleToClients)
        .flatMap(KSClassDeclaration::findVisibleTypes)

    return functionTypes + propertyTypes + companionTypes
}

// A companion that is a facade is checked as a facade, not as a part of its class.
private fun KSClassDeclaration.findCompanions() = this.declarations
    .filterIsInstance<KSClassDeclaration>()
    .filter(KSClassDeclaration::isCompanionObject)
    .filter { companion -> companion.findFacadeAnnotation() == null }

internal fun KSFunctionDeclaration.findSignatureTypes() = this
    .findTakenTypes()
    .plus(this.returnType)
    .filterNotNull()

// A parameter that declares a property is checked once, as the property.
private fun KSFunctionDeclaration.findTakenTypes() = this.parameters
    .filterNot(KSValueParameter::isProperty)
    .map(KSValueParameter::type)
    .plus(this.extensionReceiver)
    .filterNotNull()
    .asSequence()

// Generated members repeat declarations checked elsewhere: properties of a data class, members of a delegate.
private fun KSDeclaration.isSynthetic() = this.origin == Origin.SYNTHETIC

private fun KSValueParameter.isProperty() = when {
    this.isVal -> true
    this.isVar -> true
    else -> false
}

internal fun KSPropertyDeclaration.findSignatureTypes() = sequenceOf(this.type, this.extensionReceiver)
    .filterNotNull()

// Java opens a protected member to its whole package, wider than a package-private one.
private fun KSDeclaration.isVisibleToClients() = when {
    this.isPrivate() -> false
    this.origin == Origin.JAVA -> true
    this.isProtected() -> false
    else -> true
}

internal fun KSTypeReference.findDependencies() = this
    .findClasses()
    .distinct()

private fun KSTypeReference.findClasses() = this.findClasses(emptySet())

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

internal fun KSClassDeclaration.findCarriedMarkers() = this
    .findMarkerSources()
    .flatMapIndexed { aggregateIndex, source ->
        source
            .findMarkers()
            .map { annotation -> SubsystemMarker(annotation, source, aggregateIndex) }
    }
    .distinctBy(SubsystemMarker::readQualifiedName)

// A visited class is skipped: an argument of a supertype may lead back to a class already walked.
private fun KSClassDeclaration.findMarkerSources(): List<KSClassDeclaration> {
    val classesByName = this
        .readQualifiedName()
        .let { name -> mutableMapOf(name to this) }

    val queue = ArrayDeque<KSClassDeclaration>().also { deque -> deque.addLast(this) }

    while (queue.isNotEmpty()) {
        queue
            .removeFirst()
            .superTypes
            .flatMap(KSTypeReference::findClasses)
            .filterNot { supertype -> supertype.readQualifiedName() in classesByName }
            .forEach { supertype ->
                supertype
                    .readQualifiedName()
                    .let { name -> classesByName[name] = supertype }

                queue.addLast(supertype)
            }
    }

    return classesByName.values.toList()
}

private fun KSAnnotated.findMarkers() = this.annotations
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
