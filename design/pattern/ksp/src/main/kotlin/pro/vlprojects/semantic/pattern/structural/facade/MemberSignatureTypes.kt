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

    private fun findFunctionTypes(function: KSFunctionDeclaration) = function.parameters
        .map(KSValueParameter::type)
        .plus(function.returnType)
        .plus(function.extensionReceiver)
        .filterNotNull()
        .asSequence()

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
