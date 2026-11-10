package pro.vlprojects.semantic.pattern.creational.registry.base

import pro.vlprojects.semantic.pattern.creational.registry.Registry
import pro.vlprojects.semantic.pattern.creational.registry.RegistryStore
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

public abstract class AbstractRegistry<T : Any>(
    private val store: RegistryStore<T>,
) : Registry<T> {

    override val instances: Map<String, T>
        get() = store.instances

    override fun find(name: String): T? = store.find(name)

    override fun register(name: String, instance: T) {
        if (name.isBlank()) {
            throw InvalidNameException(name)
        }
        if (!store.putIfAbsent(name, instance)) {
            throw InstanceAlreadyExistsException(name)
        }
    }

    override fun replace(name: String, instance: T): T = store.getAndReplace(name, instance)
        ?: throw InstanceNotFoundException(name)

    override fun unregister(name: String): T = store.getAndRemove(name)
        ?: throw InstanceNotFoundException(name)
}
