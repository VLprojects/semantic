package pro.vlprojects.semantic.pattern.creational.registry.error

public class InstanceNotFoundException(
    public val name: String,
) : NoSuchElementException("No instance is registered under '$name'")
