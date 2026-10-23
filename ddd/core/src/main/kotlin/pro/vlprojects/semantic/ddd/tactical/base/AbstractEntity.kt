package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.dsl.check
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier
import kotlin.reflect.KClass

public abstract class AbstractEntity<E : Entity<E, I>, I : Identifier> : Entity<E, I> {
    protected open val invariants: Iterable<BusinessRule<E>>
        get() = emptyList()

    protected fun E.change(block: E.() -> Unit): E =
        apply {
            block()
            invariants.check(this)
        }

    // The JPA module overrides it to unwrap Hibernate proxies.
    protected open fun classOf(entity: Any): KClass<*> = entity::class

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other == null || classOf(this) != classOf(other) -> false
            else -> identifier == (other as Entity<*, *>).identifier
        }

    override fun hashCode(): Int = identifier.hashCode()

    override fun toString(): String = "${classOf(this).simpleName}[id: $identifier]"
}
