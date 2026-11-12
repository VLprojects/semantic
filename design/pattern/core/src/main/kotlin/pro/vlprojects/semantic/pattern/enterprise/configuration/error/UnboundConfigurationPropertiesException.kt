package pro.vlprojects.semantic.pattern.enterprise.configuration.error

public class UnboundConfigurationPropertiesException(
    prefix: String,
    public val keys: Set<String>,
) : DefinitionParsingException(prefix, "Unbound configuration properties $keys under '$prefix'")
