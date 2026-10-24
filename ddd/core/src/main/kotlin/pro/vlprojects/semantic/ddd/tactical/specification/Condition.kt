package pro.vlprojects.semantic.ddd.tactical.specification

import pro.vlprojects.semantic.ddd.tactical.Specification

public class Condition<in T, V> internal constructor(
    public val path: PropertyPath<T, V>,
    public val constraint: Constraint<V>,
) : Specification<T> {

    override fun isSatisfiedBy(candidate: T): Boolean = path.valueOf(candidate).let(constraint::isSatisfiedBy)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Condition<*, *> -> false
        else -> path == other.path && constraint == other.constraint
    }

    override fun hashCode(): Int = 31 * path.hashCode() + constraint.hashCode()

    override fun toString(): String = "$path $constraint"
}
