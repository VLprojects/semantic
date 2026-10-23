package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.dsl.check
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier
import kotlin.reflect.KClass

/**
 * An [Entity] that keeps its invariants, equal to another of the same class with the same identifier, whatever their
 * state.
 *
 * A change goes through [change]: the block changes the state, and the invariants are checked once it is done, so the
 * entity is never left broken.
 *
 * Holds no state of its own: the subclass declares the identifier, so that a persistence module can map it the way it
 * needs. The class of an entity is taken through [classOf], which the JPA module overrides to see through Hibernate
 * proxies, so that [equals], [toString] and the names built from the class stay the same for a proxy.
 *
 * ```kotlin
 * class Line(override val identifier: LineId, var quantity: Int) : AbstractEntity<Line, LineId>() {
 *     override val invariants: List<BusinessRule<Line>> get() = INVARIANTS
 *
 *     fun increase(by: Int): Line = change { quantity += by }
 * }
 * ```
 */
public abstract class AbstractEntity<E : Entity<E, I>, I : Identifier> : Entity<E, I> {
    /** The rules the entity keeps after every [change]; none by default. */
    protected open val invariants: Iterable<BusinessRule<E>>
        get() = emptyList()

    /** Runs [block] on the entity, then checks the [invariants], so that a change cannot break them. */
    protected fun E.change(block: E.() -> Unit): E =
        apply {
            block()
            invariants.check(this)
        }

    /** The class of [entity] as the model sees it; a persistence module overrides it to unwrap its proxies. */
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
