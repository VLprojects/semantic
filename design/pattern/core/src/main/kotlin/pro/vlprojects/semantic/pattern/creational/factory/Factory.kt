package pro.vlprojects.semantic.pattern.creational.factory

import pro.vlprojects.semantic.pattern.creational.factory.error.InvalidDefinitionException

public fun interface Factory<D : Definition, T : Any> {

    @Throws(InvalidDefinitionException::class)
    public fun create(definition: D): T
}
