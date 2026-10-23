package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.dsl.check
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier

public abstract class AbstractEntity<E : Entity<E, I>, I : Identifier> : Entity<E, I> {
    abstract override val identifier: I

    protected open val invariants: Iterable<BusinessRule<E>>
        get() = emptyList()

    protected fun E.change(block: E.() -> Unit): E = apply {
        block()
        invariants.check(this)
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || this::class != other::class -> false
        else -> identifier == (other as Entity<*, *>).identifier
    }

    override fun hashCode(): Int = identifier.hashCode()

    override fun toString(): String = "${this::class.simpleName}[id: $identifier]"
}
