package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer
import java.util.UUID

class PrefixedUuidKeyDeserializer : KeyDeserializer() {

    override fun deserializeKey(key: String, context: DeserializationContext) = key
        .removePrefix("U-")
        .let(UUID::fromString)
}
