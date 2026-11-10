package pro.vlprojects.semantic.pattern.creational.factory

import pro.vlprojects.semantic.pattern.creational.factory.error.InvalidDefinitionException
import kotlin.reflect.KClass

public interface Factory<D : Factory.Definition, out T : Any> {

    public val definitionType: KClass<D>

    @Throws(InvalidDefinitionException::class)
    public fun create(definition: D): T

    public interface Definition
}
