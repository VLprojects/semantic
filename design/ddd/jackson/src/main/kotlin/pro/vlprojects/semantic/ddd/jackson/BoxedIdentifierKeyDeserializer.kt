package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.KeyDeserializer

internal class BoxedIdentifierKeyDeserializer(private val identifierType: JavaType) : KeyDeserializer() {

    @Volatile
    private var resolvedKeyDeserializer: KeyDeserializer? = null

    override fun resolve(context: DeserializationContext) {
        resolvedKeyDeserializer = requireKeyDeserializer(context)
    }

    override fun deserializeKey(key: String?, context: DeserializationContext) = this
        .getOrResolveKeyDeserializer(context)
        .deserializeKey(key, context)

    // A wrapper from a ValueDeserializerModifier may not pass resolve on.
    private fun getOrResolveKeyDeserializer(context: DeserializationContext) = resolvedKeyDeserializer
        ?: this
            .requireKeyDeserializer(context)
            .also { keyDeserializer -> resolvedKeyDeserializer = keyDeserializer }

    private fun requireKeyDeserializer(context: DeserializationContext) = context
        .findCreatorKeyDeserializer(identifierType)
        ?: context.requireStringBasedKeyDeserializer(identifierType)
}
