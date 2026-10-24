package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

internal fun <T> List<Specification<T>>.allOf() = flatMap { it.conjuncts() }
    .filterNot { it == Always }
    .let { parts ->
        when {
            Never in parts -> Never
            parts.isEmpty() -> Always
            parts.size == 1 -> parts.single()
            else -> AllOf(parts)
        }
    }

internal fun <T> List<Specification<T>>.anyOf() = flatMap { it.disjuncts() }
    .filterNot { it == Never }
    .let { parts ->
        when {
            Always in parts -> Always
            parts.isEmpty() -> Never
            parts.size == 1 -> parts.single()
            else -> AnyOf(parts)
        }
    }

internal fun <T> Specification<T>.negate() = when (this) {
    is Not -> specification
    Always -> Never
    Never -> Always
    else -> Not(this)
}

private fun <T> Specification<T>.conjuncts() = when (this) {
    is AllOf -> specifications
    else -> listOf(this)
}

private fun <T> Specification<T>.disjuncts() = when (this) {
    is AnyOf -> specifications
    else -> listOf(this)
}
