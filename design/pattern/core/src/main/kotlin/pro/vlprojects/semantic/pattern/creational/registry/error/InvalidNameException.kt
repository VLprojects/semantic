package pro.vlprojects.semantic.pattern.creational.registry.error

public class InvalidNameException(
    public val name: String,
) : IllegalArgumentException("The name of an instance is blank: '$name'")
