package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public abstract class InvalidConfigurationException(
    public val name: String,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
