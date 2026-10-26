package pro.vlprojects.semantic.ddd.spring.data.jpa.integration

import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier
import jakarta.persistence.MappedSuperclass

// Not AbstractId of the core: JPA builds a key by a constructor with no arguments, and AbstractId has none.
@MappedSuperclass
abstract class LongId(
    override val value: Long,
) : BoxedIdentifier<Long> {

    override fun equals(other: Any?) = when {
        this === other -> true
        other == null -> false
        this::class != other::class -> false
        else -> value == (other as LongId).value
    }

    override fun hashCode() = value.hashCode()

    override fun toString() = value.toString()
}
