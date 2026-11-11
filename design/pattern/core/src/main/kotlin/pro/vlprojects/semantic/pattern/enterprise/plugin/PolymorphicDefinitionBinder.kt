package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleTypesException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.TypeNotFoundException

public interface PolymorphicDefinitionBinder<out D : Factory.Definition> {

    @Throws(TypeNotFoundException::class, MultipleTypesException::class)
    public fun bind(
        name: String,
        properties: Map<String, String>,
        defaults: Map<String, String> = emptyMap(),
    ): D
}
