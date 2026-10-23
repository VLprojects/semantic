package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.dsl.check
import pro.vlprojects.semantic.ddd.support.abbreviated
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier

public abstract class AbstractEntity<E : Entity<E, I>, I : Identifier> : Entity<E, I> {

    final override lateinit var identifier: I
        protected set

    protected open val invariants: List<BusinessRule<E>>
        get() = emptyList()

    protected fun E.change(mutation: E.() -> Unit): E = apply {
        mutation()
        invariants.check(this)
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null -> false
        this::class != other::class -> false
        else -> identifier == (other as Entity<*, *>).identifier
    }

    override fun hashCode(): Int = identifier.hashCode()

    override fun toString(): String = "${this::class.simpleName} [entity][id: ${identifier.toString().abbreviated()}]"
}
