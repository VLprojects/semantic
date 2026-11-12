package pro.vlprojects.semantic.pattern.enterprise.configuration.error

public class UnknownConfigurationException(
    prefix: String,
    public val typeName: String,
) : InvalidConfigurationException(prefix, "Unknown configuration '$typeName' under '$prefix'")
