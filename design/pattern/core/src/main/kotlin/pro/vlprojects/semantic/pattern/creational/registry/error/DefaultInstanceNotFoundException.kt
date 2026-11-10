package pro.vlprojects.semantic.pattern.creational.registry.error

public class DefaultInstanceNotFoundException(
    name: String,
) : InstanceNotFoundException(name, "No default instance is registered under '$name'")
