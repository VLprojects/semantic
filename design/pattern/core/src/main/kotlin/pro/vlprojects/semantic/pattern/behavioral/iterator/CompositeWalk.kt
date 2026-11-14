package pro.vlprojects.semantic.pattern.behavioral.iterator

import pro.vlprojects.semantic.pattern.structural.composite.Composite

/**
 * A lazy walk over a hierarchy from its start: each call of [iterator] walks anew, each setting returns
 * a new walk. It enters an element only if [Composite.hasChildren], so an empty container is a leaf.
 */
public interface CompositeWalk<C : Composite<C>> : Sequence<C> {

    /** Called for an element before it and its children are given; `false` skips it with its children. */
    public fun onEnter(function: (C) -> Boolean): CompositeWalk<C>

    /** Called for an entered element after it and its children are given. */
    public fun onLeave(function: (C) -> Unit): CompositeWalk<C>

    /**
     * Unlimited by default. The start is at depth 0; an element at [depth] is given as a leaf.
     * Throws [IllegalArgumentException] unless [depth] is positive.
     */
    public fun maxDepth(depth: Int): CompositeWalk<C>
}
