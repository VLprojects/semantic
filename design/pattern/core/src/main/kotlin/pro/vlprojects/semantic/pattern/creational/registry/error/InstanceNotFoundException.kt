package pro.vlprojects.semantic.pattern.creational.registry.error

public open class InstanceNotFoundException(
    public val name: String,
    message: String = "No instance is registered under '$name'",
) : NoSuchElementException(message)
