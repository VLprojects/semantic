package pro.vlprojects.semantic.pattern.creational.registry

public interface Registry<T : Any> {

    public val names: Set<String>

    public fun find(name: String): T?

    public operator fun get(name: String): T = find(name)
        ?: throw NoSuchElementException("No instance is registered under '$name'")

    public operator fun contains(name: String): Boolean = name in names

    public fun register(name: String, instance: T)

    public fun register(name: String, supplier: () -> T)

    public fun replace(name: String, instance: T)

    public fun replace(name: String, supplier: () -> T)

    public fun unregister(name: String)
}
