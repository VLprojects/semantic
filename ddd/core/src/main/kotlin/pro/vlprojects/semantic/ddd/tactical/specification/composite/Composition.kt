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
    return parts.toSpecification(Always, ::AllOf)
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
    return parts.toSpecification(Never, ::AnyOf)
}

internal fun <T> Specification<T>.negate() = when (this) {
    is Not -> specification
    Always -> Never
    Never -> Always
    else -> Not(this)
}

private fun <T> List<Specification<T>>.toSpecification(
    neutral: Specification<T>,
    composite: (List<Specification<T>>) -> Specification<T>,
) = when (size) {
    0 -> neutral
    1 -> first()
    else -> composite(this)
}
