package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

internal fun <T> Iterable<Specification<T>>.conjoin() = flatMap(Specification<T>::unwrapAllOf)
    .filterNot { it == Always }
    .let { parts ->
        when {
            Never in parts -> Never
            parts.isEmpty() -> Always
            parts.size == 1 -> parts.single()
            else -> AllOf(parts)
        }
    }

internal fun <T> Iterable<Specification<T>>.disjoin() = flatMap(Specification<T>::unwrapAnyOf)
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

private fun <T> Specification<T>.unwrapAllOf() = when (this) {
    is AllOf -> specifications
    else -> listOf(this)
}

private fun <T> Specification<T>.unwrapAnyOf() = when (this) {
    is AnyOf -> specifications
    else -> listOf(this)
}
