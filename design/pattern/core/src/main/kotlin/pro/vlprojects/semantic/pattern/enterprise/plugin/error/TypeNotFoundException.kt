package pro.vlprojects.semantic.pattern.enterprise.plugin.error

public class TypeNotFoundException(
    public val name: String,
    public val typeName: String,
) : NoSuchElementException("No type '$typeName' is known for '$name'")
