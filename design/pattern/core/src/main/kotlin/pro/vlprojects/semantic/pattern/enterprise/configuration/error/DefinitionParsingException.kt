package pro.vlprojects.semantic.pattern.enterprise.configuration.error

public abstract class DefinitionParsingException(
    prefix: String,
    message: String,
) : InvalidConfigurationException(prefix, message)
