package pro.vlprojects.semantic.pattern.behavioral.resolver

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

internal fun Class<*>.collectTypeVariableMap() = this
    .collectAllGenericSupertypes()
    .flatMap { supertype ->
        supertype
            .collectOwnerTypes()
            .plus(supertype)
    }
    .filterIsInstance<ParameterizedType>()
    .flatMap(ParameterizedType::zipTypeParametersWithArguments)
    .toMap()

private fun Type.collectAllGenericSupertypes(): List<Type> = this
    .toRawClassOrNull()
    ?.listGenericSupertypes()
    .orEmpty()
    .flatMap { supertype ->
        supertype
            .collectAllGenericSupertypes()
            .plus(supertype)
    }

private fun Class<*>.listGenericSupertypes() = this.genericSuperclass
    .let(::listOfNotNull)
    .plus(this.genericInterfaces)

private fun Type.collectOwnerTypes(): List<Type> = (this as? ParameterizedType)
    ?.ownerType
    ?.let { owner ->
        owner
            .collectOwnerTypes()
            .plus(owner)
    }
    .orEmpty()

private fun ParameterizedType.zipTypeParametersWithArguments() = this
    .toRawClassOrNull()
    ?.typeParameters
    .orEmpty()
    .zip(this.actualTypeArguments)

internal fun Map<out TypeVariable<*>, Type>.resolveRawClassOrNull(type: Type) = this
    .resolveRawClassOrNull(type, emptySet())

private fun Map<out TypeVariable<*>, Type>.resolveRawClassOrNull(
    type: Type,
    visitedVariables: Set<TypeVariable<*>>,
): Class<*>? = when (type) {
    is GenericArrayType -> this.resolveRawClassOrNull(type.genericComponentType, visitedVariables)?.arrayType()
    is TypeVariable<*> -> this.resolveVariableOrNull(type, visitedVariables)
    else -> type.toRawClassOrNull()
}

private fun Map<out TypeVariable<*>, Type>.resolveVariableOrNull(
    variable: TypeVariable<*>,
    visitedVariables: Set<TypeVariable<*>>,
) = when (variable) {
    in visitedVariables -> null

    else -> this[variable]?.let { argument ->
        visitedVariables
            .plus(variable)
            .let { visited -> this.resolveRawClassOrNull(argument, visited) }
    }
}

private fun Type.toRawClassOrNull(): Class<*>? = when (this) {
    is Class<*> -> this
    is ParameterizedType -> this.rawType.toRawClassOrNull()
    else -> null
}
