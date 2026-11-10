package pro.vlprojects.semantic.pattern.creational.registry

public interface RegistryStore<T : Any> {

    public fun find(name: String): T?

    public fun putIfAbsent(name: String, instance: T): Boolean

    public fun getAndReplace(name: String, instance: T): T?

    public fun getAndRemove(name: String): T?

    public fun getAll(): Collection<T>
}
