package pro.vlprojects.semantic.pattern.enterprise.configuration.error

public abstract class InvalidConfigurationException(
    public val prefix: String,
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
