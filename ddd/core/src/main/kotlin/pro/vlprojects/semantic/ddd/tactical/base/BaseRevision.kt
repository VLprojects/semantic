package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.support.abbreviated
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.Revision
import java.util.Objects

public class BaseRevision<out I : Identifier>(
    override val name: String,
    override val identifier: I,
    override val version: Long,
) : Revision<I> {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is BaseRevision<*> -> false
        else -> name == other.name && identifier == other.identifier && version == other.version
    }

    override fun hashCode(): Int = Objects.hash(name, identifier, version)

    override fun toString(): String = "[name: $name, id: ${identifier.toString().abbreviated()}, version: $version]"
}
