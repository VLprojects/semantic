package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.ValueInstantiator
import tools.jackson.databind.deser.jdk.JDKKeyDeserializers

internal fun DeserializationContext.findCreatorKeyDeserializer(identifierType: JavaType) = this
    .findValueInstantiator(identifierType)
    ?.buildCreatorKeyDeserializer(this)

internal fun DeserializationContext.requireStringBasedKeyDeserializer(identifierType: JavaType) = this
    .findStringBasedKeyDeserializer(identifierType)
    ?: this.reportBadDefinition(identifierType, "No creator or String factory of $identifierType reads a key")

// Not findRootValueDeserializer: it wraps a polymorphic type and hides the instantiator.
private fun DeserializationContext.findValueInstantiator(identifierType: JavaType) = this
    .findNonContextualValueDeserializer(identifierType)
    .let { deserializer -> deserializer as? ValueInstantiator.Gettable }
    ?.valueInstantiator

// Outside resolve, Jackson does not turn this failure into a bad definition itself.
private fun DeserializationContext.findStringBasedKeyDeserializer(identifierType: JavaType) = try {
    JDKKeyDeserializers.findStringBasedKeyDeserializer(this, identifierType)
} catch (exception: IllegalArgumentException) {
    this.reportBadDefinition(identifierType, exception.message)
}
