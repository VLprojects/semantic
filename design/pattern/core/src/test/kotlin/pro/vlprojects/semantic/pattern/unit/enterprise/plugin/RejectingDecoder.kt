package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.PropertiesDecoder
import kotlin.reflect.KClass

class RejectingDecoder(private val failure: Throwable) : PropertiesDecoder {

    override fun <T : Any> decode(properties: Map<String, String>, type: KClass<T>) = throw failure
}
