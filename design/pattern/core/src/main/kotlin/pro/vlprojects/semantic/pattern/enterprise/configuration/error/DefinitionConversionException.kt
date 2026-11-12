package pro.vlprojects.semantic.pattern.enterprise.configuration.error

import pro.vlprojects.semantic.pattern.enterprise.configuration.Definition
import kotlin.reflect.KClass

public class DefinitionConversionException(
    public val definition: Definition,
    public val type: KClass<*>,
    cause: Throwable? = null,
) : RuntimeException("Failed to convert the definition $definition to ${type.qualifiedName}", cause)
