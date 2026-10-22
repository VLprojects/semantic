package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.Identifier

/** An [AggregateRevision] that holds what it is given. */
public class SimpleAggregateRevision<out I : Identifier>(
    override val name: String,
    override val identifier: I,
    override val version: Long,
) : AggregateRevision<I> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleAggregateRevision<*>) return false
        return name == other.name && identifier == other.identifier && version == other.version
    }

    override fun hashCode(): Int = identifier.hashCode() * 31 + version.hashCode()

    override fun toString(): String = "$name[$identifier v$version]"
}
