package pro.vlprojects.semantic.pattern.behavioral.resolver.error

public class NotSubtypeException(
    public val className: String,
    public val baseTypeName: String,
) : IllegalArgumentException("The class '$className' is not a subtype of '$baseTypeName'")
