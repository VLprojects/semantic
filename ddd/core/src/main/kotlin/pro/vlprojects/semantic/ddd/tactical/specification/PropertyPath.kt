package pro.vlprojects.semantic.ddd.tactical.specification

import kotlin.reflect.KProperty1

public class PropertyPath<in T, out V> internal constructor(
    public val properties: List<KProperty1<*, *>>,
) {

    public val names: List<String>
        get() = properties.map { it.name }

    @Suppress("UNCHECKED_CAST")
    public fun valueOf(candidate: T): V = properties.fold<KProperty1<*, *>, Any?>(candidate) { value, property ->
        value?.let((property as KProperty1<Any, Any?>)::get)
    } as V

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PropertyPath<*, *> -> false
        else -> properties == other.properties
    }

    override fun hashCode(): Int = properties.hashCode()

    override fun toString(): String = names.joinToString(".")
}
