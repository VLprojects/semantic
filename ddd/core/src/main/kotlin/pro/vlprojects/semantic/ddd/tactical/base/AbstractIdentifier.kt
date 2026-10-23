package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.Identifier

/**
 * An [Identifier] of one [value], equal to another of the same class with the same value.
 *
 * A class rather than a value class, since a value class cannot extend one: take it where the identifier has to be a
 * class, such as a JPA embedded id. Elsewhere a value class implementing [Identifier] costs less.
 *
 * ```kotlin
 * class OrderId(value: UUID) : AbstractIdentifier<UUID>(value)
 * ```
 */
public abstract class AbstractIdentifier<out T : Any>(
    public val value: T,
) : Identifier {
    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other == null || this::class != other::class -> false
            else -> value == (other as AbstractIdentifier<*>).value
        }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}
