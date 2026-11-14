package pro.vlprojects.semantic.pattern.structural.composite

/** The base type of a hierarchy: a container overrides [children], a leaf keeps none. */
public interface Composite<C : Composite<C>> {

    public val children: Collection<C>
        get() = emptyList()

    public fun hasChildren(): Boolean = children.isNotEmpty()
}
