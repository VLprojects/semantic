package pro.vlprojects.semantic.pattern.enterprise.configuration

import kotlin.reflect.KClass

public interface ConfigurationDefinitionDecoder {

    /**
     * Decodes the arguments of [definition] to an object of [type]: no arguments give its defaults;
     * a missing required, an extra or a wrong argument throws.
     */
    public fun <T : Any> decode(definition: ConfigurationDefinition, type: KClass<T>): T
}
