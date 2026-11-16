package pro.vlprojects.semantic.pattern.structural.facade

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Origin
import pro.vlprojects.semantic.ksp.helper.getDeclaredClasses
import pro.vlprojects.semantic.ksp.helper.hasAnnotation
import pro.vlprojects.semantic.ksp.helper.isSynthetic

internal class MemberSignatureTypes private constructor(private val declaration: KSClassDeclaration) {

    fun findVisible(): Sequence<KSTypeReference> {
        val functionTypes = this.declaration
            .getDeclaredFunctions()
            .filter(::isVisibleToClients)
            .filterNot(KSFunctionDeclaration::isConstructor)
            .flatMap(::findFunctionTypes)

        val propertyTypes = this.declaration
            .getDeclaredProperties()
            .filter(::isVisibleToClients)
            .flatMap(::findPropertyTypes)

        val companionTypes = this
            .findCompanions()
            .filter(::isVisibleToClients)
            .map(Factory::of)
            .flatMap(MemberSignatureTypes::findVisible)

        return functionTypes + propertyTypes + companionTypes
    }

    fun findAll() = this.findTaken() + this.findReturned()

    // Generated members repeat declarations checked elsewhere: properties of a data class, members of a delegate.
    fun findTaken(): Sequence<KSTypeReference> {
        val functionTypes = this.declaration
            .getDeclaredFunctions()
            .filterNot(KSFunctionDeclaration::isSynthetic)
            .flatMap(::findParameterTypes)

        val propertyTypes = this.declaration
            .getDeclaredProperties()
            .flatMap(::findPropertyTypes)

        val companionTypes = this
            .findCompanions()
            .map(Factory::of)
            .flatMap(MemberSignatureTypes::findTaken)

        return functionTypes + propertyTypes + companionTypes
    }

    private fun findReturned(): Sequence<KSTypeReference> {
        val functionTypes = this.declaration
            .getDeclaredFunctions()
            .filterNot(KSFunctionDeclaration::isSynthetic)
            .filterNot(KSFunctionDeclaration::isConstructor)
            .mapNotNull(KSFunctionDeclaration::returnType)

        val companionTypes = this
            .findCompanions()
            .map(Factory::of)
            .flatMap(MemberSignatureTypes::findReturned)

        return functionTypes + companionTypes
    }

    private fun findFunctionTypes(function: KSFunctionDeclaration) = this
        .findParameterTypes(function)
        .plus(function.returnType)
        .filterNotNull()

    // A parameter that declares a property is checked once, as the property.
    private fun findParameterTypes(function: KSFunctionDeclaration) = function.parameters
        .filterNot(::isProperty)
        .map(KSValueParameter::type)
        .plus(function.extensionReceiver)
        .filterNotNull()
        .asSequence()

    private fun isProperty(parameter: KSValueParameter) = when {
        parameter.isVal -> true
        parameter.isVar -> true
        else -> false
    }

    private fun findPropertyTypes(property: KSPropertyDeclaration) =
        sequenceOf(property.type, property.extensionReceiver).filterNotNull()

    // A companion that is a facade is checked as a facade, not as a part of its class.
    private fun findCompanions() = this.declaration
        .getDeclaredClasses()
        .filter(KSClassDeclaration::isCompanionObject)
        .filterNot { companion -> companion.hasAnnotation(Facade::class) }

    // Java opens a protected member to its whole package, wider than a package-private one.
    private fun isVisibleToClients(member: KSDeclaration) = when {
        member.isPrivate() -> false
        member.origin == Origin.JAVA -> true
        member.isProtected() -> false
        else -> true
    }

    companion object Factory {

        fun of(declaration: KSClassDeclaration) = MemberSignatureTypes(declaration)
    }
}
