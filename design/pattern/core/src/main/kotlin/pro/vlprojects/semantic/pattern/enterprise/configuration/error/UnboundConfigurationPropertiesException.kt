package pro.vlprojects.semantic.pattern.enterprise.configuration.error

public class UnboundConfigurationPropertiesException(
    prefix: String,
    keys: Set<String>,
) : DefinitionParsingException(prefix, "Unbound configuration properties $keys under '$prefix'") {

    public val keys: Set<String> = keys.toSet()
}
