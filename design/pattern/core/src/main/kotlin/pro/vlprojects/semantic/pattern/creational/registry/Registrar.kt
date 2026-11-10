package pro.vlprojects.semantic.pattern.creational.registry

public fun interface Registrar<T : Any> {

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    public fun populate(registry: Registry<T>)
}
