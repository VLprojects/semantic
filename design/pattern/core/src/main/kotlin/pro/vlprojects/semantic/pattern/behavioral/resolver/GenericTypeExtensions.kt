package pro.vlprojects.semantic.pattern.behavioral.resolver

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

internal fun Class<*>.resolveTypeParameterOrNull(typeParameter: TypeVariable<*>) = this
    .resolveTypeParameterOrNull(typeParameter, emptyMap())

private fun Type.resolveTypeParameterOrNull(
    typeParameter: TypeVariable<*>,
    subtypeMappings: Map<out TypeVariable<*>, Class<*>>,
): Class<*>? = this
    .collectTypeMappings(subtypeMappings)
    .let { mappings ->
        mappings[typeParameter]
            ?: this.resolveTypeParameterInSupertypesOrNull(typeParameter, mappings)
    }

private fun Type.resolveTypeParameterInSupertypesOrNull(
    typeParameter: TypeVariable<*>,
    mappings: Map<out TypeVariable<*>, Class<*>>,
) = this
    .toRawClassOrNull()
    ?.listGenericSupertypes()
    .orEmpty()
    .firstNotNullOfOrNull { supertype -> supertype.resolveTypeParameterOrNull(typeParameter, mappings) }

private fun Class<*>.listGenericSupertypes() = this.genericSuperclass
    .let(::listOfNotNull)
    .plus(this.genericInterfaces)

private fun Type.collectTypeMappings(subtypeMappings: Map<out TypeVariable<*>, Class<*>>) = this
    .collectOwnerTypes()
    .plus(this)
    .filterIsInstance<ParameterizedType>()
    .flatMap(ParameterizedType::zipTypeParametersWithArguments)
    .mapNotNull { (variable, argument) ->
        subtypeMappings
            .resolveRawClassOrNull(argument)
            ?.let { rawClass -> variable to rawClass }
    }
    .toMap()

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

private fun Map<out TypeVariable<*>, Class<*>>.resolveRawClassOrNull(type: Type): Class<*>? = when (type) {
    is GenericArrayType -> this.resolveRawClassOrNull(type.genericComponentType)?.arrayType()
    is TypeVariable<*> -> this[type]
    else -> type.toRawClassOrNull()
}

private fun Type.toRawClassOrNull(): Class<*>? = when (this) {
    is Class<*> -> this
    is ParameterizedType -> this.rawType.toRawClassOrNull()
    else -> null
}
