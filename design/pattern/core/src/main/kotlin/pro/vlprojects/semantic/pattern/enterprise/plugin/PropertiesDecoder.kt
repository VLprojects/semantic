package pro.vlprojects.semantic.pattern.enterprise.plugin

import kotlin.reflect.KClass

public interface PropertiesDecoder {

    /**
     * Decodes [properties], keyed relative to the object as the configuration spells them, `[0]` for an index,
     * to an object of [type]: empty [properties] give its defaults; a required field missing, an extra
     * or a wrong one throws.
     */
    public fun <T : Any> decode(properties: Map<String, String>, type: KClass<T>): T
}
