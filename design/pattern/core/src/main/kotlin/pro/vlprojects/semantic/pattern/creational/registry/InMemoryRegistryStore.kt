package pro.vlprojects.semantic.pattern.creational.registry

import java.util.concurrent.ConcurrentHashMap

public class InMemoryRegistryStore<T : Any> : RegistryStore<T> {

    private val instances = ConcurrentHashMap<String, T>()

    override fun find(name: String): T? = instances[name]

    override fun putIfAbsent(name: String, instance: T): T? = instances.putIfAbsent(name, instance)

    override fun replace(name: String, instance: T): T? = instances.replace(name, instance)

    override fun remove(name: String): T? = instances.remove(name)
}
