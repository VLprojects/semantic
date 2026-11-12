package pro.vlprojects.semantic.pattern.enterprise.configuration

import kotlin.reflect.KClass

public interface DefinitionMapper {

    /**
     * Converts the arguments of [definition] to an object of [type]: no arguments give its defaults;
     * a missing required, an extra or a wrong argument throws [IllegalArgumentException].
     */
    @Throws(IllegalArgumentException::class)
    public fun <T : Any> convert(definition: Definition, type: KClass<T>): T
}
