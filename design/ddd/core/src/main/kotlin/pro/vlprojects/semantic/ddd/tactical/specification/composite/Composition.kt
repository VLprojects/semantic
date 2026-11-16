package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

public fun <T> allOf(vararg specifications: Specification<T>?): Specification<T> = specifications.asList().allOf()

public fun <T> anyOf(vararg specifications: Specification<T>?): Specification<T> = specifications.asList().anyOf()

// O(m) over the arguments: nested nodes are kept as they are and flattened on first use.
public fun <T> Iterable<Specification<T>?>.allOf(): Specification<T> {
    val children = mutableListOf<Specification<T>>()
    for (part in this) {
        when (part) {
            null, Always -> Unit
            Never -> return Never
            else -> children.add(part)
        }
    }
    return when (children.size) {
        0 -> Always
        1 -> children.first()
        else -> AllOf(children)
    }
}

// O(m) over the arguments: nested nodes are kept as they are and flattened on first use.
public fun <T> Iterable<Specification<T>?>.anyOf(): Specification<T> {
    val children = mutableListOf<Specification<T>>()
    for (part in this) {
        when (part) {
            null, Never -> Unit
            Always -> return Always
            else -> children.add(part)
        }
    }
    return when (children.size) {
        0 -> Never
        1 -> children.first()
        else -> AnyOf(children)
    }
}

internal fun <T> Specification<T>.negate() = when (this) {
    is Not -> specification
    Always -> Never
    Never -> Always
    else -> Not(this)
}

// O(n) time and memory with a stack of its own, so a chain of any depth does not overflow the call stack.
internal fun <T> List<Specification<T>>.flatten(childrenOf: (Specification<T>) -> List<Specification<T>>?) =
    ArrayDeque(this.asReversed()).let { pending ->
        buildList {
            while (pending.isNotEmpty()) {
                val specification = pending.removeLast()
                when (val children = childrenOf(specification)) {
                    null -> add(specification)
                    else -> pending.addAll(children.asReversed())
                }
            }
        }
    }
