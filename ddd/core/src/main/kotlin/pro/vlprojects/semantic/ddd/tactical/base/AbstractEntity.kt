package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier

/**
 * An [Entity] equal to another of the same class with the same identifier, whatever their state.
 *
 * Holds no state of its own: the subclass declares the identifier, so that a persistence module can map it the way it
 * needs. The JPA module overrides [equals] to see through Hibernate proxies.
 *
 * ```kotlin
 * class Line(override val identifier: LineId, var quantity: Int) : AbstractEntity<Line, LineId>()
 * ```
 */
public abstract class AbstractEntity<E : AbstractEntity<E, I>, I : Identifier> : Entity<E, I> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return identifier == (other as AbstractEntity<*, *>).identifier
    }

    override fun hashCode(): Int = identifier.hashCode()

    override fun toString(): String = "${javaClass.simpleName}[$identifier]"
}
