package pro.vlprojects.semantic.pattern.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

public interface Registry<T : Any> {

    public fun find(name: String): T?

    @Throws(InstanceNotFoundException::class)
    public operator fun get(name: String): T = find(name)
        ?: throw InstanceNotFoundException(name)

    public operator fun contains(name: String): Boolean = find(name) != null

    @Throws(InvalidNameException::class, InstanceAlreadyExistsException::class)
    public fun register(name: String, instance: T)

    @Throws(InstanceNotFoundException::class)
    public fun replace(name: String, instance: T): T

    @Throws(InstanceNotFoundException::class)
    public fun unregister(name: String): T
}
