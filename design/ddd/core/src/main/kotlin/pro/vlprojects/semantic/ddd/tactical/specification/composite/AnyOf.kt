package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.helper.collection.hasSameElementsAs
import pro.vlprojects.semantic.helper.collection.sumHashCodes
import kotlin.LazyThreadSafetyMode.PUBLICATION

public class AnyOf<in T> internal constructor(
    internal val children: List<Specification<T>>,
) : Specification<T> {

    // O(1) per call: summed once from the hashes the children already hold.
    private val hash = children.sumHashCodes()

    // O(n) time and memory on first use only; a race may flatten twice, which is cheaper than a lock.
    public val specifications: List<Specification<T>> by lazy(PUBLICATION, ::flattenChildren)

    override fun isSatisfiedBy(candidate: T): Boolean = specifications.any { it.isSatisfiedBy(candidate) }

    // O(1) when the hashes differ, otherwise O(n) time and memory.
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is AnyOf<*> -> false
        hash != other.hash -> false
        else -> specifications.hasSameElementsAs(other.specifications)
    }

    override fun hashCode(): Int = hash

    override fun toString(): String = specifications.joinToString(" or ", "(", ")")

    private fun flattenChildren() = children.flatten { (it as? AnyOf<T>)?.children }
}
