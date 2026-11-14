package pro.vlprojects.semantic.pattern.structural.composite

/**
 * A container whose children change after it is made; only a container implements it, so a leaf
 * has no [add] or [remove] to refuse. [toImmutable] makes the element of the hierarchy that keeps
 * the children of this moment, since only the hierarchy knows its immutable class; the copy is
 * shallow, so a mutable child stays mutable in it.
 */
public interface MutableComposite<C : Composite<C>> : Composite<C> {

    override val children: MutableCollection<C>

    public fun add(child: C): MutableComposite<C> = apply { children.add(child) }

    public fun remove(child: C): MutableComposite<C> = apply { children.remove(child) }

    public fun toImmutable(): C
}
