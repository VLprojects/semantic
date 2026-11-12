package pro.vlprojects.semantic.pattern.behavioral.resolver.error

import kotlin.reflect.KClass

public class NotSubtypeException(
    public val className: String,
    public val baseType: KClass<*>,
) : IllegalArgumentException("The class '$className' is not a subtype of ${baseType.java.name}")
