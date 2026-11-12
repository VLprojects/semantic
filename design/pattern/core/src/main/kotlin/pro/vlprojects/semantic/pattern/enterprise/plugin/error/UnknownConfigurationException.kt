package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class UnknownConfigurationException(
    name: String,
    public val typeName: String,
) : InvalidConfigurationException(name, "Unknown configuration '$typeName' in '$name'")
