package pro.vlprojects.semantic.ddd.dsl

import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.AllOf
import pro.vlprojects.semantic.ddd.tactical.specification.Always
import pro.vlprojects.semantic.ddd.tactical.specification.AnyOf
import pro.vlprojects.semantic.ddd.tactical.specification.Never
import pro.vlprojects.semantic.ddd.tactical.specification.Not

public infix fun <T> Specification<T>.and(other: Specification<T>): Specification<T> = listOf(this, other).allOf()

public operator fun <T> Specification<T>.not(): Specification<T> = when (this) {
    is Not -> specification
    Always -> Never
    Never -> Always
    else -> Not(this)
}

public fun <T> allOf(vararg specifications: Specification<T>): Specification<T> = specifications.asList().allOf()

public fun <T> anyOf(vararg specifications: Specification<T>): Specification<T> = specifications.asList().anyOf()

public fun <T> Iterable<Specification<T>>.allOf(): Specification<T> = flatMap { it.conjuncts() }
    .filterNot { it == Always }
    .let { parts ->
        when {
            Never in parts -> Never
            parts.isEmpty() -> Always
            parts.size == 1 -> parts.single()
            else -> AllOf(parts)
        }
    }

public fun <T> Iterable<Specification<T>>.anyOf(): Specification<T> = flatMap { it.disjuncts() }
    .filterNot { it == Never }
    .let { parts ->
        when {
            Always in parts -> Always
            parts.isEmpty() -> Never
            parts.size == 1 -> parts.single()
            else -> AnyOf(parts)
        }
    }

public fun <T> Specification<T>.remainderUnsatisfiedBy(candidate: T): Specification<T> = when {
    isSatisfiedBy(candidate) -> {
        Always
    }

    this is AllOf -> {
        specifications
            .map { it.remainderUnsatisfiedBy(candidate) }
            .allOf()
    }

    else -> {
        this
    }
}

private fun <T> Specification<T>.conjuncts() = when (this) {
    is AllOf -> specifications
    else -> listOf(this)
}

private fun <T> Specification<T>.disjuncts() = when (this) {
    is AnyOf -> specifications
    else -> listOf(this)
}
