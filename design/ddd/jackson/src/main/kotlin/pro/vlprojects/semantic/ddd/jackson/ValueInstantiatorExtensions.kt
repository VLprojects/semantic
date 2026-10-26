package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.ValueInstantiator

// A key is a string, so a creator of a String goes first, as it does when Jackson reads a string of JSON.
internal fun ValueInstantiator.buildCreatorKeyDeserializer(context: DeserializationContext) = when {
    this.canCreateFromString() -> this.buildScalarCreatorKeyDeserializer(context, ::createFromString)
    this.canCreateUsingDelegate() -> this.buildDelegatingKeyDeserializer(context)
    this.canCreateFromLong() -> this.buildScalarCreatorKeyDeserializer(context, ::createFromLong)
    this.canCreateFromInt() -> this.buildScalarCreatorKeyDeserializer(context, ::createFromInt)
    this.canCreateFromDouble() -> this.buildScalarCreatorKeyDeserializer(context, ::createFromDouble)
    this.canCreateFromBoolean() -> this.buildScalarCreatorKeyDeserializer(context, ::createFromBoolean)
    else -> null
}

private fun ValueInstantiator.buildDelegatingKeyDeserializer(context: DeserializationContext) = this
    .getDelegateType(context.config)
    .let { valueType -> this.buildKeyDeserializerOfValue(context, valueType, ::createUsingDelegate) }

// Jackson keeps a creator of a String, an int, a long, a double or a boolean apart from the delegating one.
private inline fun <reified T : Any> ValueInstantiator.buildScalarCreatorKeyDeserializer(
    context: DeserializationContext,
    noinline create: (DeserializationContext, T) -> Any?,
) = T::class.java
    .let(context::constructType)
    .let { valueType ->
        this.buildKeyDeserializerOfValue(context, valueType) { creationContext, value ->
            create(creationContext, value as T)
        }
    }

private fun ValueInstantiator.buildKeyDeserializerOfValue(
    context: DeserializationContext,
    valueType: JavaType,
    create: IdentifierCreator,
) = context
    .findKeyDeserializer(valueType, null)
    .let { valueKeyDeserializer -> CreatorKeyDeserializer(this.valueClass, valueKeyDeserializer, create) }
