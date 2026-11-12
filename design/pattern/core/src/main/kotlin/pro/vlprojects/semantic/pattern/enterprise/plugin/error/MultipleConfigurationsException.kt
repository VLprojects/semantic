package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class MultipleConfigurationsException(
    prefix: String,
    public val typeNames: Set<String>,
) : InvalidConfigurationException(prefix, "Multiple configurations $typeNames under '$prefix'")
