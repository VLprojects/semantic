package pro.vlprojects.semantic.pattern.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceNotFoundException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

public abstract class AbstractRegistry<T : Any>(
    protected val store: RegistryStore<T>,
) : Registry<T> {

    override fun find(name: String): T? = store.find(name)

    override fun register(name: String, instance: T) {
        if (name.isBlank()) {
            throw InvalidNameException(name)
        }
        if (store.putIfAbsent(name, instance) != null) {
            throw InstanceAlreadyExistsException(name)
        }
    }

    override fun replace(name: String, instance: T): T = store.replace(name, instance)
        ?: throw InstanceNotFoundException(name)

    override fun unregister(name: String): T = store.remove(name)
        ?: throw InstanceNotFoundException(name)
}
