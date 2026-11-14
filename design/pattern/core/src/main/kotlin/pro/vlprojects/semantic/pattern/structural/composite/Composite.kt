package pro.vlprojects.semantic.pattern.structural.composite

/**
 * The base type of a hierarchy whose containers and leaves a client treats alike: a container
 * overrides [children], a leaf keeps none; the operation of the hierarchy stays in its own classes.
 */
public interface Composite<C : Composite<C>> {

    public val children: Collection<C>
        get() = emptyList()

    public fun hasChildren(): Boolean = children.isNotEmpty()
}
