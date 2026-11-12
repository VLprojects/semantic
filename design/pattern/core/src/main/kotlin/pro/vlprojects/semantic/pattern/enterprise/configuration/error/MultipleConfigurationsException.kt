package pro.vlprojects.semantic.pattern.enterprise.configuration.error

public class MultipleConfigurationsException(
    prefix: String,
    public val typeNames: Set<String>,
) : DefinitionParsingException(prefix, "Multiple configurations $typeNames under '$prefix'")
