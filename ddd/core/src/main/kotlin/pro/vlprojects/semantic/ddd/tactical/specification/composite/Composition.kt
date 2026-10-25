package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

internal fun <T> Iterable<Specification<T>?>.conjoin(): Specification<T> = buildList {
    for (part in this@conjoin) {
        when (part) {
            null, Always -> Unit
            Never -> return Never
            is AllOf -> addAll(part.specifications)
            else -> add(part)
        }
    }
}.collapse(Always, ::AllOf)

internal fun <T> Iterable<Specification<T>?>.disjoin(): Specification<T> = buildList {
    for (part in this@disjoin) {
        when (part) {
            null, Never -> Unit
            Always -> return Always
            is AnyOf -> addAll(part.specifications)
            else -> add(part)
        }
    }
}.collapse(Never, ::AnyOf)

internal fun <T> Specification<T>.negate() = when (this) {
    is Not -> specification
    Always -> Never
    Never -> Always
    else -> Not(this)
}

private fun <T> List<Specification<T>>.collapse(
    empty: Specification<T>,
    node: (List<Specification<T>>) -> Specification<T>,
) = when (size) {
    0 -> empty
    1 -> first()
    else -> node(this)
}
