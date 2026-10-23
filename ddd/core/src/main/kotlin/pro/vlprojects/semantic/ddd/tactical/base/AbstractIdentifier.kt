package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.Identifier

// For a JPA embedded id, which cannot be a value class.
public abstract class AbstractIdentifier<out T : Any>(
    public val value: T,
) : Identifier {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null -> false
        this::class != other::class -> false
        else -> value == (other as AbstractIdentifier<*>).value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}
