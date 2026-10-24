package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.support.hasSameElementsAs
import pro.vlprojects.semantic.ddd.support.unorderedHashCode
import pro.vlprojects.semantic.ddd.tactical.Specification

public class AnyOf<in T> internal constructor(
    public val specifications: List<Specification<T>>,
) : Specification<T> {

    override fun isSatisfiedBy(candidate: T): Boolean = specifications.any { it.isSatisfiedBy(candidate) }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is AnyOf<*> -> false
        else -> specifications.hasSameElementsAs(other.specifications)
    }

    override fun hashCode(): Int = specifications.unorderedHashCode()

    override fun toString(): String = specifications.joinToString(" or ", "(", ")")
}
