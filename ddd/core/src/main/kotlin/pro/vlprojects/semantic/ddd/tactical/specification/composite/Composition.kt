package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

internal fun <T> Iterable<Specification<T>?>.conjoin(): Specification<T> {
    val parts = mutableListOf<Specification<T>>()
    for (part in this) {
        when (part) {
            null, Always -> Unit
            Never -> return Never
            is AllOf -> parts.addAll(part.specifications)
            else -> parts.add(part)
        }
    }
    return when (parts.size) {
        0 -> Always
        1 -> parts.first()
        else -> AllOf(parts)
    }
}

internal fun <T> Iterable<Specification<T>?>.disjoin(): Specification<T> {
    val parts = mutableListOf<Specification<T>>()
    for (part in this) {
        when (part) {
            null, Never -> Unit
            Always -> return Always
            is AnyOf -> parts.addAll(part.specifications)
            else -> parts.add(part)
        }
    }
    return when (parts.size) {
        0 -> Never
        1 -> parts.first()
        else -> AnyOf(parts)
    }
}

internal fun <T> Specification<T>.negate() = when (this) {
    is Not -> specification
    Always -> Never
    Never -> Always
    else -> Not(this)
}
