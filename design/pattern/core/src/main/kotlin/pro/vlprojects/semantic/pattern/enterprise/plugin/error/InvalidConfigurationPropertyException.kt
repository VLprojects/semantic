package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class InvalidConfigurationPropertyException(
    prefix: String,
    cause: Throwable,
) : InvalidConfigurationException(prefix, "Invalid configuration property under '$prefix'", cause)
