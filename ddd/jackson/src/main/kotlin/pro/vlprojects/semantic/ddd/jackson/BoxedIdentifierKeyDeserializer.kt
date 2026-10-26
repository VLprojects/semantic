package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.KeyDeserializer

internal class BoxedIdentifierKeyDeserializer(private val type: JavaType) : KeyDeserializer() {

    private lateinit var keyDeserializer: KeyDeserializer

    override fun resolve(ctxt: DeserializationContext) {
        keyDeserializer = ctxt.findCreatorKeyDeserializer(type)
            ?: ctxt.requireStringBasedKeyDeserializer(type)
    }

    override fun deserializeKey(key: String, ctxt: DeserializationContext) = keyDeserializer.deserializeKey(key, ctxt)
}
