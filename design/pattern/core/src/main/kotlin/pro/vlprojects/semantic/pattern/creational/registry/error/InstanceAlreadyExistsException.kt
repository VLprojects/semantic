package pro.vlprojects.semantic.pattern.creational.registry.error

public class InstanceAlreadyExistsException(
    public val name: String,
) : IllegalStateException("An instance is already registered under '$name'")
