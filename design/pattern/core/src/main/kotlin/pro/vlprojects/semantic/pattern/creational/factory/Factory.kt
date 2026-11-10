package pro.vlprojects.semantic.pattern.creational.factory

import pro.vlprojects.semantic.pattern.creational.factory.error.InvalidDefinitionException

public interface Factory<in D : Factory.Definition, out T : Any> {

    public val definitionType: Class<@UnsafeVariance D>

    @Throws(InvalidDefinitionException::class)
    public fun create(definition: D): T

    public interface Definition
}
