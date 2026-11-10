package pro.vlprojects.semantic.pattern.creational.registry

public fun interface Registrar<T : Any> {

    public fun populate(registry: Registry<T>)
}
