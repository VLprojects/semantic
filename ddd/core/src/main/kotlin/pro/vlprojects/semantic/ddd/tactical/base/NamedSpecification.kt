package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.dsl.ConditionScope
import pro.vlprojects.semantic.ddd.tactical.Specification

public abstract class NamedSpecification<in T>(
    define: ConditionScope<T>.() -> Specification<T>,
) : Specification<T> {

    public val definition: Specification<T> = ConditionScope<T>().define()

    final override fun isSatisfiedBy(candidate: T): Boolean = definition.isSatisfiedBy(candidate)

    final override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is NamedSpecification<*> -> false
        else -> this::class == other::class && definition == other.definition
    }

    final override fun hashCode(): Int = 31 * this::class.hashCode() + definition.hashCode()

    override fun toString(): String = this::class.simpleName ?: definition.toString()
}
