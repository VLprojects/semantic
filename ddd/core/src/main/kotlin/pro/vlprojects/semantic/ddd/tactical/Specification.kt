package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.dsl.allOf
import pro.vlprojects.semantic.ddd.dsl.anyOf
import pro.vlprojects.semantic.ddd.tactical.specification.composite.negate

public interface Specification<in T> :
    DomainObject,
    (T) -> Boolean {

    public fun isSatisfiedBy(candidate: T): Boolean

    override fun invoke(candidate: T): Boolean = isSatisfiedBy(candidate)

    public infix fun and(other: Specification<@UnsafeVariance T>): Specification<T> = allOf(this, other)

    public infix fun or(other: Specification<@UnsafeVariance T>): Specification<T> = anyOf(this, other)

    public operator fun not(): Specification<T> = negate()
}
