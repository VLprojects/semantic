package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class InvalidConfigurationPropertyException(
    name: String,
    cause: Throwable,
) : InvalidConfigurationException(name, "Invalid configuration property in '$name'", cause)
