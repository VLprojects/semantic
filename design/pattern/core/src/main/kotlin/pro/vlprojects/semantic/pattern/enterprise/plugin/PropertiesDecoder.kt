package pro.vlprojects.semantic.pattern.enterprise.plugin

import kotlin.reflect.KClass

public interface PropertiesDecoder {

    /**
     * Decodes [properties] to an object of [type]; keys are relative to it: `jitter.factor`, `statuses[0]`.
     * Empty [properties] give the defaults of [type]; a missing required, an extra or a wrong field throws.
     */
    public fun <T : Any> decode(properties: Map<String, String>, type: KClass<T>): T
}
