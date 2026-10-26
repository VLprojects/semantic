package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.KeyDeserializer
import tools.jackson.databind.deser.ValueInstantiator
import tools.jackson.databind.deser.jdk.JDKKeyDeserializers

internal fun DeserializationContext.findDelegatingCreatorKeyDeserializer(type: JavaType) = this
    .findValueInstantiator(type)
    ?.takeIf(ValueInstantiator::canCreateUsingDelegate)
    ?.let { instantiator -> this.createKeyDeserializer(instantiator) }

internal fun DeserializationContext.requireStringBasedKeyDeserializer(type: JavaType) = JDKKeyDeserializers
    .findStringBasedKeyDeserializer(this, type)
    ?: this.reportBadDefinition(type, "Cannot find a (Map) Key deserializer for type $type")

// A root deserializer of a polymorphic type is wrapped and hides its instantiator.
private fun DeserializationContext.findValueInstantiator(type: JavaType) = this
    .findNonContextualValueDeserializer(type)
    .let { deserializer -> deserializer as? ValueInstantiator.Gettable }
    ?.valueInstantiator

private fun DeserializationContext.createKeyDeserializer(instantiator: ValueInstantiator) = instantiator
    .getDelegateType(this.config)
    .let { valueType -> this.findKeyDeserializer(valueType, null) }
    .let { valueKeyDeserializer -> DelegatingCreatorKeyDeserializer(instantiator, valueKeyDeserializer) }
