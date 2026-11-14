package pro.vlprojects.semantic.pattern.enterprise.configuration

import pro.vlprojects.semantic.pattern.enterprise.configuration.error.DefinitionConversionException
import kotlin.reflect.KClass

public interface DefinitionMapper {

    /**
     * Converts the arguments of [definition] to an object of [targetType]: no arguments give its defaults;
     * a missing required, an extra or a wrong argument throws [DefinitionConversionException].
     */
    @Throws(DefinitionConversionException::class)
    public fun <T : Any> convert(definition: Definition, targetType: KClass<T>): T
}
