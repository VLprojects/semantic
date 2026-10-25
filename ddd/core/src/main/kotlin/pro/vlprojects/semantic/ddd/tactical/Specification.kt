package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.tactical.specification.composite.conjunction
import pro.vlprojects.semantic.ddd.tactical.specification.composite.disjunction
import pro.vlprojects.semantic.ddd.tactical.specification.composite.negate

public interface Specification<in T> :
    DomainObject,
    (T) -> Boolean {

    public fun isSatisfiedBy(candidate: T): Boolean

    override fun invoke(candidate: T): Boolean = isSatisfiedBy(candidate)

    public infix fun and(other: Specification<@UnsafeVariance T>): Specification<T> = listOf(this, other).conjunction()

    public infix fun or(other: Specification<@UnsafeVariance T>): Specification<T> = listOf(this, other).disjunction()

    public operator fun not(): Specification<T> = negate()
}
