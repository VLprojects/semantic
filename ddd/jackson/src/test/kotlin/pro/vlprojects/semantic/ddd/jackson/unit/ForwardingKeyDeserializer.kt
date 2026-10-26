package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer

class ForwardingKeyDeserializer(private val keyDeserializer: KeyDeserializer) : KeyDeserializer() {

    override fun deserializeKey(key: String, context: DeserializationContext) = keyDeserializer
        .deserializeKey(key, context)
}
