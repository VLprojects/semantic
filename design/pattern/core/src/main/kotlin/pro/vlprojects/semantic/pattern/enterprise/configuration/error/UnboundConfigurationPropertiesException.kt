package pro.vlprojects.semantic.pattern.enterprise.configuration.error

public class UnboundConfigurationPropertiesException(
    public val keys: Set<String>,
) : IllegalArgumentException("Unbound configuration properties $keys")
