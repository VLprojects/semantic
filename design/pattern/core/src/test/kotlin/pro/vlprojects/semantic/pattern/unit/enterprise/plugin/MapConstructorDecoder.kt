package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.PropertiesDecoder
import kotlin.reflect.KClass

object MapConstructorDecoder : PropertiesDecoder {

    override fun <T : Any> decode(properties: Map<String, String>, type: KClass<T>) = type.java
        .getConstructor(Map::class.java)
        .newInstance(properties)
}
