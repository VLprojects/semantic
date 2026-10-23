package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.typeName

public class Revision<out I : Identifier>(
    override val name: String,
    override val identifier: I,
    override val version: Long,
) : AggregateRevision<I> {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Revision<*> -> false
        else -> name == other.name && identifier == other.identifier && version == other.version
    }

    override fun hashCode(): Int = identifier.hashCode() * 31 + version.hashCode()

    override fun toString(): String = "$name[$identifier v$version]"

    public companion object {

        public fun <I : Identifier> of(aggregate: AggregateRoot<*, I>): Revision<I> = Revision(
            name = aggregate.typeName,
            identifier = aggregate.identifier,
            version = aggregate.version,
        )
    }
}
