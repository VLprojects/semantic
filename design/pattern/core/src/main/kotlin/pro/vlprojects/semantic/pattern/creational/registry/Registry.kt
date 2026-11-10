package pro.vlprojects.semantic.pattern.creational.registry

public interface Registry<T : Any> {

    public val names: Set<String>

    public fun find(name: String): T?

    @Throws(NoSuchElementException::class)
    public operator fun get(name: String): T = find(name)
        ?: throw NoSuchElementException("No instance is registered under '$name'")

    public operator fun contains(name: String): Boolean = name in names

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    public fun register(name: String, instance: T)

    @Throws(IllegalArgumentException::class, NoSuchElementException::class)
    public fun replace(name: String, instance: T): T

    @Throws(IllegalArgumentException::class, NoSuchElementException::class)
    public fun unregister(name: String): T
}
