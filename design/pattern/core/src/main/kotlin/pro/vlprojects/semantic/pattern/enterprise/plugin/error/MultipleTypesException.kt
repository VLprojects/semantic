package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class MultipleTypesException(
    public val name: String,
    public val typeNames: Set<String>,
) : IllegalArgumentException("Several types are set in '$name': $typeNames")
