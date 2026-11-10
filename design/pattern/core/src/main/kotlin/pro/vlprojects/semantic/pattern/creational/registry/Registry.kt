package pro.vlprojects.semantic.pattern.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.error.DefaultInstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

public interface Registry<T : Any> : AutoCloseable {

    public fun find(name: String): T?

    @Throws(InstanceNotFoundException::class)
    public fun resolve(name: String): T = find(name)
        ?: throw InstanceNotFoundException(name)

    @Throws(DefaultInstanceNotFoundException::class)
    public fun resolveDefault(): T = find(DEFAULT_NAME)
        ?: throw DefaultInstanceNotFoundException(DEFAULT_NAME)

    @Throws(DefaultInstanceNotFoundException::class)
    public fun resolveOrDefault(name: String): T = find(name)
        ?: resolveDefault()

    public operator fun contains(name: String): Boolean = find(name) != null

    public fun getAll(): Collection<T>

    @Throws(InvalidNameException::class, InstanceAlreadyExistsException::class)
    public fun register(name: String, instance: T)

    @Throws(InstanceNotFoundException::class)
    public fun replace(name: String, instance: T): T

    @Throws(InstanceNotFoundException::class)
    public fun unregister(name: String): T

    public companion object {

        public const val DEFAULT_NAME: String = "default"
    }
}
