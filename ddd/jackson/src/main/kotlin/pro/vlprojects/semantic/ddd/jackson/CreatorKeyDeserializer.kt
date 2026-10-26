package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer

internal class CreatorKeyDeserializer(
    private val valueKeyDeserializer: KeyDeserializer,
    private val create: (DeserializationContext, Any) -> Any?,
) : KeyDeserializer() {

    override fun deserializeKey(key: String, ctxt: DeserializationContext) = valueKeyDeserializer
        .deserializeKey(key, ctxt)
        .let { value -> create(ctxt, value) }
}
