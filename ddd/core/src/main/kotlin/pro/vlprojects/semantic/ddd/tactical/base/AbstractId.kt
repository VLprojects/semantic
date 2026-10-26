package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier
import java.io.Serializable

public abstract class AbstractId<out T : Serializable>(
    override val value: T,
) : BoxedIdentifier<T> {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null -> false
        this::class != other::class -> false
        else -> value == (other as AbstractId<*>).value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}
