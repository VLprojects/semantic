package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.ValueInstantiator
import tools.jackson.databind.deser.jdk.JDKKeyDeserializers

internal fun DeserializationContext.findCreatorKeyDeserializer(type: JavaType) = this
    .findValueInstantiator(type)
    ?.let { instantiator -> this.chooseCreatorKeyDeserializer(instantiator) }

internal fun DeserializationContext.requireStringBasedKeyDeserializer(type: JavaType) = JDKKeyDeserializers
    .findStringBasedKeyDeserializer(this, type)
    ?: this.reportBadDefinition(type, "Cannot find a (Map) Key deserializer for type $type")

// A root deserializer of a polymorphic type is wrapped and hides its instantiator.
private fun DeserializationContext.findValueInstantiator(type: JavaType) = this
    .findNonContextualValueDeserializer(type)
    .let { deserializer -> deserializer as? ValueInstantiator.Gettable }
    ?.valueInstantiator

// Jackson keeps a creator of an int, a long, a double or a boolean apart from the delegating one.
private fun DeserializationContext.chooseCreatorKeyDeserializer(instantiator: ValueInstantiator) = when {
    instantiator.canCreateUsingDelegate() -> this.createDelegatingKeyDeserializer(instantiator)
    instantiator.canCreateFromLong() -> this.createScalarKeyDeserializer(instantiator::createFromLong)
    instantiator.canCreateFromInt() -> this.createScalarKeyDeserializer(instantiator::createFromInt)
    instantiator.canCreateFromDouble() -> this.createScalarKeyDeserializer(instantiator::createFromDouble)
    instantiator.canCreateFromBoolean() -> this.createScalarKeyDeserializer(instantiator::createFromBoolean)
    else -> null
}

private fun DeserializationContext.createDelegatingKeyDeserializer(instantiator: ValueInstantiator) = instantiator
    .getDelegateType(this.config)
    .let { valueType -> this.createKeyDeserializer(valueType, instantiator::createUsingDelegate) }

private inline fun <reified T : Any> DeserializationContext.createScalarKeyDeserializer(
    noinline create: (DeserializationContext, T) -> Any?,
) = T::class.java
    .let(::constructType)
    .let { valueType ->
        this.createKeyDeserializer(valueType) { ctxt, value -> create(ctxt, value as T) }
    }

private fun DeserializationContext.createKeyDeserializer(
    valueType: JavaType,
    create: (DeserializationContext, Any) -> Any?,
) = this
    .findKeyDeserializer(valueType, null)
    .let { valueKeyDeserializer -> CreatorKeyDeserializer(valueKeyDeserializer, create) }
