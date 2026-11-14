package pro.vlprojects.semantic.pattern.structural.composite

/** The base type of a hierarchy: a container overrides [parts], a leaf keeps none. */
public interface Composite<C : Composite<C>> {

    public val parts: Collection<C>
        get() = emptyList()

    public fun hasParts(): Boolean = parts.isNotEmpty()
}
