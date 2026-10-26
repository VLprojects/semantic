package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer
import tools.jackson.databind.deser.ValueInstantiator

internal class DelegatingCreatorKeyDeserializer(
    private val instantiator: ValueInstantiator,
    private val valueKeyDeserializer: KeyDeserializer,
) : KeyDeserializer() {

    override fun deserializeKey(key: String, ctxt: DeserializationContext) = valueKeyDeserializer
        .deserializeKey(key, ctxt)
        .let { value -> instantiator.createUsingDelegate(ctxt, value) }
}
