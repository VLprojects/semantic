package pro.vlprojects.semantic.pattern.creational.factory.error

public open class InvalidDefinitionException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
