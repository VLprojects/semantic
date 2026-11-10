package pro.vlprojects.semantic.pattern.creational.registry

import java.util.concurrent.ConcurrentHashMap

public class InMemoryRegistryStore<T : Any> : RegistryStore<T> {

    private val instancesByName = ConcurrentHashMap<String, T>()

    override val instances: Map<String, T>
        get() = instancesByName.toMap()

    override fun find(name: String): T? = instancesByName[name]

    override fun putIfAbsent(name: String, instance: T): Boolean = instancesByName.putIfAbsent(name, instance) == null

    override fun getAndReplace(name: String, instance: T): T? = instancesByName.replace(name, instance)

    override fun getAndRemove(name: String): T? = instancesByName.remove(name)
}
