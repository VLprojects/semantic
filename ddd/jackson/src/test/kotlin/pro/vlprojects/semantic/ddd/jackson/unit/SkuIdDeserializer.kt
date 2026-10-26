package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer

class SkuIdDeserializer : StdDeserializer<SkuId>(SkuId::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) = p.string.let(::SkuId)
}
