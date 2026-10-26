package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.JavaType
import tools.jackson.databind.KeyDeserializer
import tools.jackson.databind.deser.ValueDeserializerModifier

class ForwardingKeyDeserializerModifier : ValueDeserializerModifier() {

    override fun modifyKeyDeserializer(
        config: DeserializationConfig,
        type: JavaType,
        deserializer: KeyDeserializer,
    ) = ForwardingKeyDeserializer(deserializer)
}
