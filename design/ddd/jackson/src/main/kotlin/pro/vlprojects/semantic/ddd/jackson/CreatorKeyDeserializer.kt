package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer
import tools.jackson.databind.exc.InvalidFormatException

private const val UNNAMED_VALUE_TYPE = "value"

internal class CreatorKeyDeserializer(
    private val identifierClass: Class<*>,
    private val valueKeyDeserializer: KeyDeserializer,
    private val create: IdentifierCreator,
) : KeyDeserializer() {

    // A null value stays a null key, as Jackson keeps one from a problem handler; NullKeySkippable is not public.
    override fun deserializeKey(key: String?, context: DeserializationContext) = key
        ?.let { text -> readValue(text, context) }
        ?.let { value -> create(context, value) }

    private fun readValue(key: String, context: DeserializationContext) = try {
        valueKeyDeserializer.deserializeKey(key, context)
    } catch (exception: InvalidFormatException) {
        val valueTypeName = exception.targetType?.simpleName ?: UNNAMED_VALUE_TYPE
        throw context
            .weirdKeyException(identifierClass, key, "not a valid $valueTypeName")
            .apply { initCause(exception) }
    }
}
