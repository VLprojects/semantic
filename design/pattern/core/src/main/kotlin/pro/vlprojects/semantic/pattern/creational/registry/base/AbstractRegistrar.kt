package pro.vlprojects.semantic.pattern.creational.registry.base

import pro.vlprojects.semantic.pattern.creational.factory.Definition
import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.registry.Registrar
import pro.vlprojects.semantic.pattern.creational.registry.Registry
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceRegistrationException

public abstract class AbstractRegistrar<D : Definition, T : Any>(
    private val factory: Factory<D, T>,
) : Registrar<T> {

    protected abstract fun loadDefinitions(): Map<String, D>

    @Throws(InstanceRegistrationException::class)
    final override fun populate(registry: Registry<T>) {
        loadDefinitions().forEach { (name, definition) ->
            try {
                definition
                    .let(factory::create)
                    .let { instance -> registry.register(name, instance) }
            } catch (failure: RuntimeException) {
                throw InstanceRegistrationException(name, failure)
            }
        }
    }
}
