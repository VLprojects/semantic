package pro.vlprojects.semantic.pattern.structural.composite

/**
 * Reads the children of an element from outside its class, as a Comparator orders from outside:
 * for a hierarchy that cannot implement [Composite].
 */
public fun interface ChildrenProvider<T> {

    public fun getChildren(element: T): Collection<T>

    public fun hasChildren(element: T): Boolean = getChildren(element).isNotEmpty()
}
