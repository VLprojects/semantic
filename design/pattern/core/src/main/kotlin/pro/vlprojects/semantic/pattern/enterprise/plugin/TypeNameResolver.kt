package pro.vlprojects.semantic.pattern.enterprise.plugin

import kotlin.reflect.KClass

public interface TypeNameResolver<out T : Any> {

    public val typeNames: Set<String>

    public fun find(typeName: String): KClass<out T>?
}
