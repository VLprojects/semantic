package pro.vlprojects.semantic.pattern.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.error.DefaultInstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

public interface Registry<T : Any> {

    public fun find(name: String): T?

    @Throws(InstanceNotFoundException::class)
    public operator fun get(name: String): T = find(name)
        ?: throw InstanceNotFoundException(name)

    @Throws(DefaultInstanceNotFoundException::class)
    public fun getDefault(): T = find(DEFAULT_NAME)
        ?: throw DefaultInstanceNotFoundException(DEFAULT_NAME)

    @Throws(DefaultInstanceNotFoundException::class)
    public fun getOrDefault(name: String): T = find(name)
        ?: getDefault()

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
