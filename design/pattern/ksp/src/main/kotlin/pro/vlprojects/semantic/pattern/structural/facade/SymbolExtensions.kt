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
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Origin
import pro.vlprojects.semantic.ksp.helper.getDeclaredClasses
import pro.vlprojects.semantic.ksp.helper.getFirstAnnotationOrNull
import pro.vlprojects.semantic.ksp.helper.getQualifiedName
import pro.vlprojects.semantic.ksp.helper.getReferencedClasses
import pro.vlprojects.semantic.ksp.helper.getShortName
import pro.vlprojects.semantic.ksp.helper.getTypeValues
import pro.vlprojects.semantic.ksp.helper.hasAnnotation
import pro.vlprojects.semantic.ksp.helper.isSynthetic
import pro.vlprojects.semantic.ksp.helper.resolveClassDeclaration

internal fun KSClassDeclaration.flattenClasses(): Sequence<KSClassDeclaration> = this
    .getDeclaredClasses()
    .flatMap(KSClassDeclaration::flattenClasses)
    .let { nested -> sequenceOf(this) + nested }

internal fun KSClassDeclaration.findSignatureTypes() = this.findTakenTypes() + this.findResultTypes()

// Generated members repeat declarations checked elsewhere: properties of a data class, members of a delegate.
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
private fun KSClassDeclaration.findCompanions() = this
    .getDeclaredClasses()
    .filter(KSClassDeclaration::isCompanionObject)
    .filterNot { companion -> companion.hasAnnotation(Facade::class) }

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

internal fun KSClassDeclaration.findCarriedMarkers() = this
    .findMarkerSources()
    .flatMapIndexed { aggregateIndex, source ->
        source
            .findMarkers()
            .map { annotation -> SubsystemMarker(annotation, source, aggregateIndex) }
    }
    .distinctBy(SubsystemMarker::getQualifiedName)

// A visited class is skipped: an argument of a supertype may lead back to a class already walked.
private fun KSClassDeclaration.findMarkerSources(): List<KSClassDeclaration> {
    val classesByName = this
        .getQualifiedName()
        .let { name -> mutableMapOf(name to this) }

    val queue = ArrayDeque<KSClassDeclaration>().also { deque -> deque.addLast(this) }

    while (queue.isNotEmpty()) {
        queue
            .removeFirst()
            .superTypes
            .flatMap(KSTypeReference::getReferencedClasses)
            .filterNot { supertype -> supertype.getQualifiedName() in classesByName }
            .forEach { supertype ->
                supertype
                    .getQualifiedName()
                    .let { name -> classesByName[name] = supertype }

                queue.addLast(supertype)
            }
    }

    return classesByName.values.toList()
}

private fun KSAnnotated.findMarkers() = this.annotations
    .filter { annotation -> annotation.resolveClassDeclaration()?.isMarker() == true }
    .distinctBy(KSAnnotation::getQualifiedName)
    .toList()

internal fun KSAnnotated.findFacadeAnnotation() = this.getFirstAnnotationOrNull(Facade::class)

internal fun KSAnnotation.hasSubsystemArguments() = this
    .getTypeValues(Facade::subsystems.name)
    .isNotEmpty()

// An unresolved class is left to the compiler, which names it better.
internal fun KSAnnotation.readSubsystems() = this
    .getTypeValues(Facade::subsystems.name)
    .filterNot(KSType::isError)
    .map(KSType::declaration)
    .map(KSDeclaration::expandTypeAlias)

internal fun KSAnnotation.getQualifiedName() = this
    .resolveClassDeclaration()
    ?.getQualifiedName()
    .orEmpty()

internal fun KSAnnotation.getShortName() = this
    .resolveClassDeclaration()
    ?.getShortName()
    .orEmpty()

internal fun KSDeclaration.isMarker() = this.hasAnnotation(Facade.Subsystem::class)

private fun KSDeclaration.expandTypeAlias() = when (this) {
    is KSTypeAlias -> this.findActualType()
    else -> this
}
