package pro.vlprojects.semantic.pattern.creational.factory

import pro.vlprojects.semantic.pattern.creational.factory.error.InvalidFactoryException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal fun Factory<*, *>.resolveDefinitionType() = this.javaClass
    .findDefinitionType(emptyMap())
    ?: throw InvalidFactoryException(this)

private fun Type.findDefinitionType(bindings: Map<Type, Type>): Class<*>? = when (this) {
    is ParameterizedType -> this.findDefinitionType(bindings)
    is Class<*> -> this.findDefinitionTypeInSupertypes(emptyMap())
    else -> null
}

private fun ParameterizedType.findDefinitionType(bindings: Map<Type, Type>): Class<*>? {
    val rawType = this.rawType as Class<*>
    val arguments = this.actualTypeArguments.map { argument -> bindings[argument] ?: argument }

    return when (rawType) {
        Factory::class.java -> arguments.first().toClass()
        else -> arguments.let(rawType::bindTypeParameters).let(rawType::findDefinitionTypeInSupertypes)
    }
}

private fun Class<*>.findDefinitionTypeInSupertypes(bindings: Map<Type, Type>) = this.genericInterfaces
    .asSequence()
    .plus(this.genericSuperclass)
    .filterNotNull()
    .firstNotNullOfOrNull { supertype -> supertype.findDefinitionType(bindings) }

private fun Class<*>.bindTypeParameters(arguments: List<Type>) = this.typeParameters
    .zip(arguments)
    .toMap<Type, Type>()

private fun Type.toClass() = when (this) {
    is Class<*> -> this
    is ParameterizedType -> this.rawType as Class<*>
    else -> null
}
