package pro.vlprojects.semantic.pattern.structural.composite

/**
 * A container whose children change; a leaf does not implement it.
 * [toImmutable] is written by the hierarchy and copies shallowly.
 */
public interface MutableComposite<C : Composite<C>> : Composite<C> {

    override val children: MutableCollection<C>

    public fun add(child: C): MutableComposite<C> = apply { children.add(child) }

    public fun remove(child: C): MutableComposite<C> = apply { children.remove(child) }

    public fun toImmutable(): C
}
