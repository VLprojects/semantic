package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import kotlin.reflect.KClass

public interface DefinitionTypeResolver<out D : Factory.Definition> {

    public val typeNames: Set<String>

    public fun find(typeName: String): KClass<out D>?
}
