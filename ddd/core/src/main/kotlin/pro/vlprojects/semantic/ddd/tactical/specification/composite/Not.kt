package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

public class Not<in T> internal constructor(
    public val specification: Specification<T>,
) : Specification<T> {

    override fun isSatisfiedBy(candidate: T): Boolean = !specification.isSatisfiedBy(candidate)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Not<*> -> false
        else -> specification == other.specification
    }

    override fun hashCode(): Int = specification.hashCode().inv()

    override fun toString(): String = "not $specification"
}
