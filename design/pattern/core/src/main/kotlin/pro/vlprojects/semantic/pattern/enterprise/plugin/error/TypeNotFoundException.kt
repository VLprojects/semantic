package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class TypeNotFoundException(
    public val name: String,
    public val typeName: String,
    public val knownTypeNames: Set<String>,
) : NoSuchElementException("No type '$typeName' is known for '$name', the known types are $knownTypeNames")
