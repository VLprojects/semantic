package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.support.hasSameElementsAs
import pro.vlprojects.semantic.ddd.support.unorderedHashCode
import pro.vlprojects.semantic.ddd.tactical.Specification
import kotlin.LazyThreadSafetyMode.PUBLICATION

public class AnyOf<in T> internal constructor(
    internal val parts: List<Specification<T>>,
) : Specification<T> {

    private val hash = parts.unorderedHashCode()

    public val specifications: List<Specification<T>> by lazy(PUBLICATION) {
        parts.flatten { (it as? AnyOf<T>)?.parts }
    }

    override fun isSatisfiedBy(candidate: T): Boolean = specifications.any { it.isSatisfiedBy(candidate) }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is AnyOf<*> -> false
        hash != other.hash -> false
        else -> specifications.hasSameElementsAs(other.specifications)
    }

    override fun hashCode(): Int = hash

    override fun toString(): String = specifications.joinToString(" or ", "(", ")")
}
