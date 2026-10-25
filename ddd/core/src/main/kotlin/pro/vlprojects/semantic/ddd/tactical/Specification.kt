package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.tactical.specification.composite.allOf
import pro.vlprojects.semantic.ddd.tactical.specification.composite.anyOf
import pro.vlprojects.semantic.ddd.tactical.specification.composite.negate

public interface Specification<in T> :
    DomainObject,
    (T) -> Boolean {

    public fun isSatisfiedBy(candidate: T): Boolean

    override fun invoke(candidate: T): Boolean = isSatisfiedBy(candidate)

    public infix fun and(other: Specification<@UnsafeVariance T>): Specification<T> = listOf(this, other).allOf()

    public infix fun or(other: Specification<@UnsafeVariance T>): Specification<T> = listOf(this, other).anyOf()

    public operator fun not(): Specification<T> = negate()
}
