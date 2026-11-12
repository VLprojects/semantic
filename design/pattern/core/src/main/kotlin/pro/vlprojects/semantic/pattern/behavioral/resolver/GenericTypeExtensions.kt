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
        when (val rawClass = this.toRawClassOrNull()) {
            typeParameter.genericDeclaration -> mappings[typeParameter]
            else -> rawClass?.resolveTypeParameterInSupertypesOrNull(typeParameter, mappings)
        }
    }

private fun Type.collectTypeMappings(subtypeMappings: Map<out TypeVariable<*>, Class<*>>) = this
    .collectOwnerTypes()
    .plus(this)
    .filterIsInstance<ParameterizedType>()
    .flatMap(ParameterizedType::zipTypeParametersWithArguments)
    .mapNotNull { (variable, argument) ->
        argument
            .resolveRawClassOrNull(subtypeMappings)
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

private fun Class<*>.resolveTypeParameterInSupertypesOrNull(
    typeParameter: TypeVariable<*>,
    mappings: Map<out TypeVariable<*>, Class<*>>,
) = this
    .listGenericSupertypes()
    .firstNotNullOfOrNull { supertype -> supertype.resolveTypeParameterOrNull(typeParameter, mappings) }

private fun Class<*>.listGenericSupertypes() = this.genericSuperclass
    .let(::listOfNotNull)
    .plus(this.genericInterfaces)

private fun Type.resolveRawClassOrNull(mappings: Map<out TypeVariable<*>, Class<*>>): Class<*>? = when (this) {
    is GenericArrayType -> this.genericComponentType.resolveRawClassOrNull(mappings)?.arrayType()
    is TypeVariable<*> -> mappings[this]
    else -> this.toRawClassOrNull()
}

private fun Type.toRawClassOrNull(): Class<*>? = when (this) {
    is Class<*> -> this
    is ParameterizedType -> this.rawType.toRawClassOrNull()
    else -> null
}
