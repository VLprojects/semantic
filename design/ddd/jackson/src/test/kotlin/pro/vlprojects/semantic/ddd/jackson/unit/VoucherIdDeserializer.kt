package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer

class VoucherIdDeserializer : StdDeserializer<VoucherId>(VoucherId::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext) = parser
        .string
        .let(VoucherId.Companion::fromText)
}
