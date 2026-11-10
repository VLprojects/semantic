package pro.vlprojects.semantic.pattern.creational.registry

public interface RegistryStore<T : Any> {

    public fun find(name: String): T?

    public fun putIfAbsent(name: String, instance: T): T?

    public fun replace(name: String, instance: T): T?

    public fun remove(name: String): T?
}
