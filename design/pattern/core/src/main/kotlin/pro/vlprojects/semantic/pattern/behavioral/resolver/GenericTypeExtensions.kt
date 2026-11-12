package pro.vlprojects.semantic.pattern.behavioral.resolver

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

internal fun Class<*>.collectTypeVariableMap() = this
    .collectAllGenericSupertypes()
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

private fun Class<*>.listGenericSupertypes() = listOfNotNull(this.genericSuperclass) + this.genericInterfaces

private fun ParameterizedType.zipTypeParametersWithArguments() = this
    .toRawClassOrNull()
    ?.typeParameters
    .orEmpty()
    .zip(this.actualTypeArguments)

internal fun Map<out TypeVariable<*>, Type>.resolveRawClassOrNull(
    type: Type,
    visitedVariables: Set<Type> = emptySet(),
): Class<*>? = when (type) {
    in visitedVariables -> null

    is GenericArrayType -> this.resolveRawClassOrNull(type.genericComponentType, visitedVariables)?.arrayType()

    is TypeVariable<*> -> this[type]?.let { argument ->
        this.resolveRawClassOrNull(argument, visitedVariables + type)
    }

    else -> type.toRawClassOrNull()
}

private fun Type.toRawClassOrNull(): Class<*>? = when (this) {
    is Class<*> -> this
    is ParameterizedType -> this.rawType.toRawClassOrNull()
    else -> null
}
