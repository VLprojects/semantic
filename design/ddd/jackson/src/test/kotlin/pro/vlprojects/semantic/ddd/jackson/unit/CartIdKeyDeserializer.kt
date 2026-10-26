package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer

class CartIdKeyDeserializer : KeyDeserializer() {

    override fun deserializeKey(key: String, context: DeserializationContext): CartId {
        key.startsWith("C-").let { isCartKey -> require(isCartKey) { "A key of a cart starts with C-" } }
        return key
            .removePrefix("C-")
            .toLong()
            .let(::CartId)
    }
}
