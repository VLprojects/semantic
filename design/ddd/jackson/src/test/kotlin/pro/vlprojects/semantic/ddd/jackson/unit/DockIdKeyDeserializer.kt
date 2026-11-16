package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer

class DockIdKeyDeserializer : KeyDeserializer() {

    override fun deserializeKey(key: String, context: DeserializationContext) = key
        .removePrefix("D-")
        .toLong()
        .let(::DockId)
}
