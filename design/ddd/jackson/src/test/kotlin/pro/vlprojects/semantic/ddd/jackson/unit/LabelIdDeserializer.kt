package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer

class LabelIdDeserializer : StdDeserializer<LabelId>(LabelId::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext) = parser
        .string
        .let(LabelId.Companion::fromString)
}
