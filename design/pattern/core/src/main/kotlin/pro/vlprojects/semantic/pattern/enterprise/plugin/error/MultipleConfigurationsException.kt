package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class MultipleConfigurationsException(
    name: String,
    public val typeNames: Set<String>,
) : InvalidConfigurationException(name, "Multiple configurations $typeNames in '$name'")
