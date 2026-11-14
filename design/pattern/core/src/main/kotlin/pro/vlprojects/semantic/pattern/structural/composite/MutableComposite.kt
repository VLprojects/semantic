package pro.vlprojects.semantic.pattern.structural.composite

/**
 * A container whose parts change; a leaf does not implement it.
 * [toImmutable] is written by the hierarchy and copies shallowly.
 */
public interface MutableComposite<C : Composite<C>> : Composite<C> {

    override val parts: MutableCollection<C>

    public fun add(part: C): MutableComposite<C> = apply { parts.add(part) }

    public fun remove(part: C): MutableComposite<C> = apply { parts.remove(part) }

    public fun toImmutable(): C
}
