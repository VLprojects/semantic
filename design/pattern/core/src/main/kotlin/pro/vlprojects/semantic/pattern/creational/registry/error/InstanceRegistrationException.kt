package pro.vlprojects.semantic.pattern.creational.registry.error

public class InstanceRegistrationException(
    public val name: String,
    cause: Throwable,
) : RuntimeException("Failed to register an instance under '$name'", cause)
