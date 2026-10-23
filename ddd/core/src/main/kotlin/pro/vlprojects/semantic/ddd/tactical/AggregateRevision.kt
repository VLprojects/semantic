package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.support.abbreviated
import java.util.Objects

public class AggregateRevision<out I : Identifier>(
    public val name: String,
    override val identifier: I,
    override val version: Long,
) : Identifiable<I>,
    Versioned {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is AggregateRevision<*> -> false
        else -> name == other.name && identifier == other.identifier && version == other.version
    }

    override fun hashCode(): Int = Objects.hash(name, identifier, version)

    override fun toString(): String = "[name: $name, id: ${identifier.toString().abbreviated()}, version: $version]"

    public companion object Factory {

        public fun <I : Identifier> of(aggregate: AggregateRoot<*, I>): AggregateRevision<I> = AggregateRevision(
            name = aggregate.typeName,
            identifier = aggregate.identifier,
            version = aggregate.version,
        )
    }
}
