package pro.vlprojects.semantic.pattern.creational.factory

public fun interface Factory<D : Any, T : Any> {

    public fun create(definition: D): T
}
